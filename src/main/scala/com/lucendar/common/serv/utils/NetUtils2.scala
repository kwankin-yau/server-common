package com.lucendar.common.serv.utils

import org.xbill.DNS.{ARecord, Address, Lookup, SimpleResolver}

import java.net.{InetAddress, UnknownHostException}
import java.util.concurrent.ConcurrentHashMap

object NetUtils2 {

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
    try {
      org.xbill.DNS.NioClient.close()
    } catch {
      case t: NullPointerException =>
        // suppress the null pointer exception when the NioClient.selector is null
    }
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

}
