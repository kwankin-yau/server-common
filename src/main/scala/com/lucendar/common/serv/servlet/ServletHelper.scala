/** *****************************************************************************
 * Copyright (c) 2019, 2021 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 * ***************************************************************************** */
package com.lucendar.common.serv.servlet

import com.google.gson.Gson
import info.gratour.common.types.rest.Reply
import org.springframework.http.ResponseEntity.HeadersBuilder
import org.springframework.http.{HttpHeaders, HttpMethod}
import org.springframework.web.servlet.config.annotation.CorsRegistry
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
 * @since 1.0.1
 */
object ServletHelper {

  val HEADER_X_AUTH_TOKEN = "X-Auth-Token"

  def addCorsHeaders(headerBuilder: HeadersBuilder[_]): Unit = {
    headerBuilder.header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
    headerBuilder.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")
    headerBuilder.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,DELETE,PUT,OPTIONS")
    headerBuilder.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*")
  }

  def addCorsHeaders(request: HttpServletRequest, headerBuilder: HeadersBuilder[_]): Unit = {
    val origin = request.getHeader(HttpHeaders.ORIGIN)
    headerBuilder.header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin)

    val requestHeaders = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS)
    if (requestHeaders != null)
      headerBuilder.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders)
    else
      headerBuilder.header(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")

    val requestMethod = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)
    if (requestMethod != null)
      headerBuilder.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod)
    else
      headerBuilder.header(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,DELETE,PUT,OPTIONS")

    val expose = request.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)
    if (expose != null)
      headerBuilder.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*")
    else
      headerBuilder.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*")

//    headerBuilder.header(HttpHeaders.VARY, "Origin")

    headerBuilder.header("Access-Control-Max-Age", "3600")
    headerBuilder.header(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
  }

  def addCorsHeaders(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val origin = request.getHeader(HttpHeaders.ORIGIN)
    if (!response.containsHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
      response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin)

    val requestHeaders = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS)
    if (requestHeaders != null)
      response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders)
    else if (!response.containsHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
      response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*")

    val requestMethod = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)
    if (requestMethod != null) response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod)
    else if (!response.containsHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
      response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,DELETE,PUT,OPTIONS")

    val expose = request.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)
    if (expose != null)
      response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*")
    else
      response.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*")

    if (!response.containsHeader(HttpHeaders.VARY))
      response.addHeader(HttpHeaders.VARY, "Origin")

    if (!response.containsHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE))
      response.setHeader("Access-Control-Max-Age", "3600")

    if (!response.containsHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
      response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
  }

  def addCorsMappings(registry: CorsRegistry): Unit = {
    registry.addMapping("/**")
      .allowedOrigins("*")
      .allowedMethods(
        HttpMethod.GET.name(),
        HttpMethod.PUT.name(),
        HttpMethod.POST.name(),
        HttpMethod.DELETE.name(),
        HttpMethod.OPTIONS.name()
      )
      .allowedHeaders(
        HEADER_X_AUTH_TOKEN,
        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
        HttpHeaders.CONTENT_TYPE,
        HttpHeaders.DATE,
        HttpHeaders.AUTHORIZATION
      )
  }

  /*
  private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR" };

private String getClientIpAddress(HttpServletRequest request) {
    for (String header : HEADERS_TO_TRY) {
        String ip = request.getHeader(header);
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
    }

    return request.getRemoteAddr();}
   */

  def getClientIp(req: HttpServletRequest): String = {
    if (req != null) {
      val addr = req.getHeader("X-Forwarded-For")
      if (addr == null || addr.isEmpty)
        req.getRemoteAddr
      else {
        if (addr.contains(","))
          addr.split(",")(0).trim
        else
          addr
      }
    } else
      null
  }

  def setErrorResp(req: HttpServletRequest, resp: HttpServletResponse, stataCode: Int, json: String): Unit = {
    resp.setStatus(stataCode)
    val bytes = json.getBytes
    resp.setContentLength(bytes.length)
    resp.getOutputStream.write(bytes)
    resp.setContentType("application/json")

    ServletHelper.addCorsHeaders(req, resp)
  }

  def setErrorResp(req: HttpServletRequest,
                   resp: HttpServletResponse,
                   stataCode: Int,
                   reply: Reply[_],
                   gson: Gson): Unit = {
    val json = gson.toJson(reply)
    setErrorResp(req, resp, stataCode, json)
  }

}
