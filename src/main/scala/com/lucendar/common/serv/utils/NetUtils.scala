/** *****************************************************************************
 * Copyright (c) 2019, 2020 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 * ***************************************************************************** */
package com.lucendar.common.serv.utils

import com.lucendar.common.utils.StringUtils
import info.gratour.common.error.ErrorWithCode
import org.apache.commons.codec.binary.Hex
import org.apache.commons.collections.map.MultiValueMap
import org.apache.commons.validator.routines.{DomainValidator, InetAddressValidator}
import org.xbill.DNS.{ARecord, Address, Lookup, SimpleResolver}

import java.net.{InetAddress, InetSocketAddress, NetworkInterface, UnknownHostException}
import java.nio.charset.{Charset, StandardCharsets}
import java.security.MessageDigest
import java.util
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

object NetUtils {

  /**
   * Determines the IP address of a host
   *
   * @param host The hostname to look up
   * @return The first matching IP address
   * @exception UnknownHostException The hostname does not have any addresses
   */
  def resolvePublicIp(host: String): InetAddress =
    Address.getByName(host)

  private final val resolverMap = new ConcurrentHashMap[String, SimpleResolver]()

  def shutdownDnsSelector(): Unit = {
    org.xbill.DNS.NioClient.close()
  }


  /**
   * Determines the IP address of a host using specified dns server
   *
   * @param host The hostname to look up
   * @param dns  the dns server
   * @return The first matching IP address
   * @exception UnknownHostException The hostname does not have any addresses
   */
  def resolvePublicIp(host: String, dns: String): InetAddress = {
    if (dns == null)
      return Address.getByName(host)

    var resolver = resolverMap.get(dns)
    if (resolver == null) {
      resolver = new SimpleResolver(dns)
      val old = resolverMap.putIfAbsent(dns, resolver)
      if (old != null)
        resolver = old
    }

    val lookup = new Lookup(host)
    lookup.setResolver(resolver)

    val records = lookup.run()
    if (records != null && records.nonEmpty) {
      records(0).asInstanceOf[ARecord].getAddress
    } else
      throw new UnknownHostException(host)
  }

  def isValidPublicIp(ip: String): Boolean = {
    var addr: InetAddress = null

    try {
      addr = InetAddress.getByName(ip)
    } catch {
      case _: UnknownHostException =>
        return false
    }

    !(addr.isSiteLocalAddress || addr.isAnyLocalAddress || addr.isLinkLocalAddress || addr.isLoopbackAddress || addr.isMulticastAddress)
  }

  def isValidIpV4Addr(ipv4: String): Boolean =
    InetAddressValidator.getInstance().isValidInet4Address(ipv4)

  def isValidIpV6Addr(ipv6: String): Boolean =
    InetAddressValidator.getInstance().isValidInet6Address(ipv6)

  def isValidIp(ip: String): Boolean =
    InetAddressValidator.getInstance().isValid(ip)

  def isLocalAddr(addr: String): Boolean = {
    try {
      // Check if the address is a valid special local or loop back
      val inetAddr = InetAddress.getByName(addr)
      if (inetAddr.isAnyLocalAddress || inetAddr.isLoopbackAddress)
        return true

      // Check if the address is defined on any interface
      NetworkInterface.getByInetAddress(inetAddr) != null
    } catch {
      case _: Throwable =>
        false
    }
  }

  def isValidPortNum(port: Int): Boolean = port > 0 && port < 65536

  final val DomainValidatorAllowLocal: DomainValidator = DomainValidator.getInstance(true)

  def isValidDomain(domain: String): Boolean = {
    DomainValidatorAllowLocal.isValid(domain)
  }

  def isValidIpOrDomain(ipOrDomain: String): Boolean = {
    if (ipOrDomain == null || ipOrDomain.isEmpty)
      false
    else {
      if (ipOrDomain.charAt(0).isDigit) {
        var r = isValidIp(ipOrDomain)
        if (!r)
          r = isValidDomain(ipOrDomain)
        r
      } else {
        var r = DomainValidatorAllowLocal.isValid(ipOrDomain)
        if (!r)
          r = isValidIp(ipOrDomain)
        r
      }

    }
  }

  /**
   * Parse a socket address string like {ip}:{port}, or {host}:{port}, support IPv6.
   *
   * @param s a socket address string like {ip}:{port}, or {host}:{port}, support IPv6.
   * @return parsed socket address. return null if parse failed.
   */
  def parseSocketAddr(s: String): InetSocketAddress = {
    try {
      val idx = s.lastIndexOf(":")
      if (idx < 0)
        return null

      val portStr = s.substring(idx + 1)
      val port = StringUtils.tryParseInt(portStr)
      if (port == null || !isValidPortNum(port))
        return null

      val hostStr = s.substring(0, idx)
      InetSocketAddress.createUnresolved(hostStr, port)
    } catch {
      case t: Throwable =>
        null
    }
  }

  case class SocketAddr(host: String, port: Int) {
    def toSocketAddress: InetSocketAddress = new InetSocketAddress(host, port)
    def toSocketAddressUnresolved: InetSocketAddress = InetSocketAddress.createUnresolved(host, port)

  }

  /**
   * Parse a socket address string like {ip}[:{port}], or {host}[:{port}] where port part is optional, support IPv6.
   *
   * @param s a socket address string like {ip}[:{port}], or {host}[:{port}] where port part is optional, support IPv6.
   * @return parsed socket address. return null if parse failed.
   */
  def parseSocketAddrPortDef(s: String, defaultPortNum: Int): SocketAddr = {
    try {
      val idx = s.lastIndexOf(":")
      if (idx < 0)
        return SocketAddr(s, defaultPortNum)

      val portStr = s.substring(idx + 1)
      val port = StringUtils.tryParseInt(portStr)
      if (port == null || !isValidPortNum(port))
        return null

      val hostStr = s.substring(0, idx)
      SocketAddr(hostStr, port)
    } catch {
      case t: Throwable =>
        null
    }
  }

  object HttpUtils {

    def calcBasicAuthorization(username: String, password: String, charset: Charset): String = {
      val bytes = (username + ":" + password).getBytes(charset)
      "Basic " + Base64.getEncoder.encodeToString(bytes)
    }

    def calcBasicAuthorization(username: String, password: String): String =
      calcBasicAuthorization(username, password, StandardCharsets.US_ASCII)

    def calcDigestAuthorization(username: String,
                                password: String,
                                realm: String,
                                nonce: String,
                                httpMethod: String,
                                digestUri: String): String = {
      // HA1 = MD5(username:realm:password)
      // HA2 = MD5(httpMethod:digestURI)
      // response = MD5(HA1:nonce:HA2)

      val md5 = MessageDigest.getInstance("MD5")
      val ha1 = Hex.encodeHexString(md5.digest((username + ":" + realm + ":" + password).getBytes))

      md5.reset()
      val ha2 = Hex.encodeHexString(md5.digest((httpMethod + ":" + digestUri).getBytes))

      md5.reset()
      val response = Hex.encodeHexString(md5.digest((ha1 + ":" + nonce + ":" + ha2).getBytes))

      // Authorization: Digest username="admin", realm="IP Camera(F3820)", nonce="f6a30073c0abd7372a8320e4ea6637bc", uri="rtsp://192.168.1.64:554/h264/ch1/main/av_stream", response="9e109a388b193cacc1bb0530b523af70"
      // Authorization: Digest username="admin", realm="IP Camera(F3820)", nonce="53c33f2ca25ae2a17df0da1625a06901", uri="rtsp://192.168.1.64:554/h264/ch1/main/av_stream", response="e7bafcb005bb3c386f4febcc8828007b"
      s"Digest username=\"${username}\", realm=\"${realm}\", nonce=\"${nonce}\", uri=\"${digestUri}\", response=\"${response}\""
    }

    case class BasicHttpAuthorization(username: String, password: String)

    case class HttpAuthorization(scheme: String, params: Array[String]) {
      def isBasic: Boolean = scheme.toLowerCase == "basic"

      def isDigest: Boolean = scheme.toLowerCase == "digest"

      def asBasic: BasicHttpAuthorization = {
        if (!isBasic)
          throw ErrorWithCode.internalError("Non Basic authorization")

        if (params == null || params.isEmpty)
          throw ErrorWithCode.invalidParam("authorization");

        val s = params(0)
        val concatenation = new String(Base64.getDecoder.decode(s), StandardCharsets.US_ASCII)
        val p = concatenation.indexOf(':')
        if (p <= 0)
          throw ErrorWithCode.invalidParam("authorization")

        BasicHttpAuthorization(concatenation.substring(0, p), concatenation.substring(p + 1))
      }
    }

    def parseAuthorization(authorization: String): HttpAuthorization = {
      val a = authorization.trim
      val p = a.indexOf(' ')
      if (p <= 0)
        throw ErrorWithCode.invalidParam("authorization")

      val scheme = a.substring(0, p)
      val s2 = a.substring(p + 1)
      val params = s2.split(",")
      HttpAuthorization(scheme, params)
    }

    def decodeQueryString(rawQueryStr: String): java.util.Map[String, String] = {
      if (rawQueryStr == null)
        return null

      val r = new util.HashMap[String, String]()
      rawQueryStr.split("&").foreach(
        s => {
          val kv = s.split("=", 2)
          if (kv.length == 2) {
            r.put(kv(0), kv(1))
          } else if (kv.length > 0) {
            r.put(kv(0), "1")
          }
        }
      )

      r
    }

    def decodeQueryStrMultiValue(rawQueryStr: String): MultiValueMap = {
      if (rawQueryStr == null)
        return null

      val r = new MultiValueMap()
      rawQueryStr.split("&").foreach(s => {
        val kv = s.split("=", 2)
        if (kv.length == 2) {
          r.put(kv(0), kv(1))
        } else if (kv.length > 0) {
          r.put(kv(0), "1")
        }
      })

      r
    }

  }


}
