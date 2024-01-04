/** *****************************************************************************
 * Copyright (c) 2019, 2021 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 * ***************************************************************************** */
package com.lucendar.common.serv.servlet


import com.google.common.base.Throwables
import com.lucendar.common.serv.utils.ServUtils
import com.typesafe.scalalogging.Logger
import info.gratour.common.error.{ErrorWithCode, Errors}
import info.gratour.common.types.rest.Reply
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.async.AsyncRequestTimeoutException

import java.sql.SQLException
import java.util.concurrent.TimeoutException

abstract class ExceptionAdvisor {

  import ExceptionAdvisor.logger

  protected def log(e: Throwable): Unit =
    logger.error(String.format("Exception occurred: [%s]%s.", e.getClass.getName, e.getMessage), e)

  protected def errorReply(errCode: Int, e: Throwable): Reply[Void] = {
    val msg = Errors.errorMessage(errCode, LocaleContextHolder.getLocale) + " " + ServUtils.rootMessageOf(e)
    Reply.error(errCode, msg)
  }

  /**
   * Translate sql state to error code.
   *
   * @param state sql state
   * @return optional error code, `None` for can not recognized
   */
  protected def sqlExceptionStateToErrorCode(state: String): Option[Int]

  protected def handleSqlException(ex: SQLException): Reply[Void] = {
    val state = ex.getSQLState

    if (state != null) {
      state match {
        case "U0001" =>
          errorReply(Errors.NOT_ENOUGH_PRIV, ex)

        case "U0002" =>
          errorReply(Errors.FOREIGN_KEY_VIOLATION, ex)

        case "U0003" =>
          errorReply(Errors.RECORD_NOT_FOUND, ex)

        case _ =>
          val err = sqlExceptionStateToErrorCode(state)
          if (err.isDefined)
            errorReply(err.get, ex)
          else
            errorReply(Errors.INTERNAL_ERROR, ex)
      }
    } else
      errorReply(Errors.INTERNAL_ERROR, ex)
  }

  @ExceptionHandler(Array(classOf[Throwable]))
  def handle(e: Throwable): Reply[Void] = {
    log(e)

    e match {
      case ec: ErrorWithCode =>
        return Reply.error(ec.getErrCode, ec.getMessage)

      case _: TimeoutException =>
        return errorReply(Errors.TIMEOUT, e)

      case _: AsyncRequestTimeoutException =>
        return errorReply(Errors.TIMEOUT, e)

      case _: HttpRequestMethodNotSupportedException =>
        return errorReply(Errors.HTTP_METHOD_NOT_SUPPORT, e)

      case _: MissingServletRequestParameterException =>
        return errorReply(Errors.MISSING_REQUEST_PARAM, e)

      case _: DuplicateKeyException =>
        return errorReply(Errors.DUPLICATED_VALUE, e)

      case _ =>
    }

    val root = Throwables.getRootCause(e)
    root match {
      case sqlEx: SQLException =>
        return handleSqlException(sqlEx)

      case _ =>
    }

    errorReply(Errors.INTERNAL_ERROR, e)
  }
}

object ExceptionAdvisor {
  private val logger = Logger[ExceptionAdvisor]



}
