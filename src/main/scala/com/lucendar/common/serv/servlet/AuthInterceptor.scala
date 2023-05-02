/** *****************************************************************************
 * Copyright (c) 2019, 2021 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 * ***************************************************************************** */
package com.lucendar.common.serv.servlet

import com.google.gson.Gson
import info.gratour.common.error.Errors
import info.gratour.common.types.rest.Reply
import org.springframework.http.{HttpStatus, MediaType}
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.UrlPathHelper

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}

case class CheckTokenResult(ok: Boolean, session: Authentication)

trait AuthInterceptor extends HandlerInterceptor {

  val gson: Gson

  val PARAM_TOKEN = "__token"
  val PARAM_DIGEST = "__digest"
  val urlPathHelper = new UrlPathHelper

  protected def getAuthToken(request: HttpServletRequest): String = {
    if (request == null)
      return null

    val r = request.getHeader(ServletHelper.HEADER_X_AUTH_TOKEN)
    if (r == null)
      request.getParameter(PARAM_TOKEN)
    else
      r
  }

  protected def setError(request: HttpServletRequest, response: HttpServletResponse, errCode: Int, status: HttpStatus): Unit = {
    val reply = Reply.error(errCode)
    val s = gson.toJson(reply)
    //    logger.debug("errorResponse=>" + s)
    response.setContentType(MediaType.APPLICATION_JSON_VALUE)
    ServletHelper.addCorsHeaders(request, response)
    response.setCharacterEncoding("UTF-8")
    response.getWriter.write(s)
    response.setStatus(status.value())
  }

  protected def checkAndUpdateToken(token: String): CheckTokenResult

  protected def ignorePaths: Array[String]

  override def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: AnyRef): Boolean = {
    SecurityContextHolder.getContext.setAuthentication(null)

    if (request.getMethod.equalsIgnoreCase("OPTIONS"))
      return true;

    val path = urlPathHelper.getPathWithinApplication(request)
    val ignores = ignorePaths
    if (ignores != null) {
      val ignored = ignores.find(p => path.startsWith(p)).orNull != null
      if (ignored)
        return true
    }

    val token = getAuthToken(request)
    if (token != null) {
      val r = checkAndUpdateToken(token)
      if (!r.ok) {
        // invalid token
        setError(request, response, Errors.INVALID_TOKEN, HttpStatus.FORBIDDEN)
        return false
      }

      SecurityContextHolder.getContext.setAuthentication(r.session)
      true
    } else {
      // no token
      setError(request, response, Errors.NOT_AUTHENTICATED, HttpStatus.FORBIDDEN)
      false
    }
  }

}
