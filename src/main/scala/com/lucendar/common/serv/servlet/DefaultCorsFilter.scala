/** *****************************************************************************
 * Copyright (c) 2019, 2020 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 * ******************************************************************************/
package com.lucendar.common.serv.servlet

import org.springframework.http.HttpHeaders

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import jakarta.servlet.{Filter, FilterChain, ServletRequest, ServletResponse}

class DefaultCorsFilter extends Filter {
  override def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain): Unit = {
    req match {
      case request: HttpServletRequest if res.isInstanceOf[HttpServletResponse] =>
        val response = res.asInstanceOf[HttpServletResponse]
        ServletHelper.addCorsHeaders(request, response)
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")

      case _ =>
    }

    chain.doFilter(req, res)
  }
}
