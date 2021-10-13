/** *****************************************************************************
 * Copyright (c) 2019, 2021 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 * ***************************************************************************** */
package com.lucendar.common.serv.servlet

import org.springframework.http.HttpHeaders

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/**
 * @since 1.0.1
 */
object ServletHelper {

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
  }

}
