/** *****************************************************************************
 * Copyright (c) 2019, 2021 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 * ***************************************************************************** */
package com.lucendar.common.serv.utils

import com.google.common.base.Throwables
import info.gratour.common.error.Errors
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FilenameUtils
import org.springframework.core.io.ResourceLoader
import org.springframework.http.{HttpStatus, MediaType}

import java.io.{IOException, InputStream}
import java.nio.ByteBuffer
import java.util.UUID

object ServUtils {

  def timeCodedId(tm: Long): String = {
    val uuid = UUID.randomUUID()
    val bytes = new Array[Byte](16 + 8)
    val bb = ByteBuffer.wrap(bytes)
    bb.putLong(tm)
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)

    Base64.encodeBase64URLSafeString(bytes)
  }

  def extractTimeFromId(id: String): Long = {
    val bytes = Base64.decodeBase64(id)
    val bb = ByteBuffer.wrap(bytes)
    bb.getLong
  }

  def uuidToBase64(uuid: UUID): String = {
    val bytes = new Array[Byte](16)
    val bb = ByteBuffer.wrap(bytes)
    bb.putLong(uuid.getMostSignificantBits)
    bb.putLong(uuid.getLeastSignificantBits)
    Base64.encodeBase64URLSafeString(bytes)
  }

  def uuidBase64: String =
    uuidToBase64(UUID.randomUUID())

  def uuidFromBase64(str: String): UUID = {
    val bytes = Base64.decodeBase64(str)
    val bb = ByteBuffer.wrap(bytes)
    new UUID(bb.getLong, bb.getLong)
  }

  case class LoadedResource(fileName: String, stream: InputStream, isLoadFromConfigDir: Boolean) extends AutoCloseable {
    override def close(): Unit = {
      if (stream != null)
        stream.close()
    }
  }

  /**
   * Load resource from config directory, if not found then fallback to class path(resource).
   * If file not found, then return null.
   *
   * @param fileName       name of file to load.
   * @param resourceLoader resource loader.
   * @return InputStream of the resource, or null if file is not found.
   */
  def tryLoadResource(fileName: String, resourceLoader: ResourceLoader): InputStream = {
    var resource = resourceLoader.getResource(s"file:config/$fileName")
    if (!resource.exists) {
      resource = resourceLoader.getResource(s"classpath:$fileName")
      if (!resource.exists)
        return null
    }

    resource.getInputStream
  }

  /**
   * Load resource from config directory, if not found then fallback to class path(resource).
   *
   * @param fileName       name of file to load.
   * @param resourceLoader resource loader.
   * @return LoadedResource object.
   * @throws IOException if both it does not found in both config directory and class path.
   */
  def loadResourceEx(fileName: String, resourceLoader: ResourceLoader): LoadedResource = {
    var resource = resourceLoader.getResource(s"file:config/$fileName")
    val isLoadFromConfigDir =
      if (!resource.exists) {
        resource = resourceLoader.getResource(s"classpath:$fileName")
        if (!resource.exists)
          throw new IOException(s"File `$fileName` was not found.")

        true
      } else
        false

    LoadedResource(fileName, resource.getInputStream, isLoadFromConfigDir)
  }

  /**
   * Load resource from config directory, if not found then fallback to class path(resource).
   *
   * @param fileName       name of file to load.
   * @param resourceLoader resource loader.
   * @return InputStream of the resource.
   * @throws IOException if both it does not found in both config directory and class path.
   */
  def loadResource(fileName: String, resourceLoader: ResourceLoader): InputStream = {
    val r = loadResourceEx(fileName, resourceLoader)
    r.stream
  }

  final val MimeTypeDefault = "application/octet-stream"
  final val MediaTypeDefault = MediaType.parseMediaType(MimeTypeDefault)

  final val FileExtMimeMap: Map[String, String] = Seq(
    "txt" -> "text/plain",

    // video
    "mp4" -> "video/mp4",
    "mpeg" -> "video/mpeg",
    "ogv" -> "video/ogg",
    "avi" -> "video/x-msvideo",
    "ts" -> "video/mp2t",
    "webm" -> "video/webm",

    // audio
    "wav" -> "audio/wav",
    "oga" -> "audio/ogg",
    "mp3" -> "audio/mpeg",
    "aac" -> "audio/aac",
    "opus" -> "audio/opus",
    "weba" -> "audio/webm",



    // image
    "png" -> "image/png",
    "jpg" -> "image/jpg",
    "jpeg" -> "image/jpg",
    "gif" -> "image/gif",
    "bmp" -> "image/bmp",
    "webp" -> "image/webp",
    "tif" -> "image/tiff",
    "tiff" -> "image/tiff",

    // html
    "htm" -> "text/html",
    "html" -> "text/html",
    "css" -> "text/css",
    "js" -> "text/javascript",
    "mjs" -> "text/javascript",
    "ttf" -> "font/ttf",
    "woff" -> "font/woff",
    "woff2" -> "font/woff2",
    "otf" -> "font/otf",

    // document
    "pdf" -> "application/pdf",
    "doc" -> "application/msword",
    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "xls" -> "application/vnd.ms-excel",
    "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "ppt" -> "application/vnd.ms-powerpoint",
    "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation",

    // data
    "json" -> "application/json",
    "xml" -> "application/xml",

    // archive
    "zip" -> "application/zip",
    "7z" -> "application/x-7z-compressed",
    "jar" -> "application/java-archive",

    "bin" -> "application/octet-stream"
  ).toMap

  val FileExtMediaTypeMap: Map[String, MediaType] = FileExtMimeMap
    .map(a => {
      (a._1, MediaType.parseMediaType(a._2))
    })

  def mapMediaTypeFromFileNameDefault(fileName: String): MediaType = {
    val ext = FilenameUtils.getExtension(fileName).toLowerCase
    FileExtMediaTypeMap.getOrElse(ext, MediaTypeDefault)
  }

  private def messageOf(t: Throwable): String =
    if (t != null) {
      val m = t.getMessage
      if (m != null)
        m
      else
        ""
    } else
      ""

  def rootMessageOf(t: Throwable): String =
    if (t != null)
      messageOf(Throwables.getRootCause(t))
    else
      ""

  def errCodeToHttpStatusCode(errCode: Int): Int = {
    errCode match {
      case Errors.OK =>
        HttpStatus.OK.value()

      case Errors.ACCESS_DENIED | Errors.NOT_ENOUGH_PRIV =>
        HttpStatus.FORBIDDEN.value()

      case Errors.NOT_AUTHENTICATED | Errors.AUTHENTICATION_FAILED | Errors.INVALID_TOKEN | Errors.SESSION_EXPIRED =>
        HttpStatus.UNAUTHORIZED.value()

      case Errors.INTERNAL_ERROR =>
        HttpStatus.INTERNAL_SERVER_ERROR.value()

      case Errors.SERVICE_UNAVAILABLE | Errors.SERVICE_BUSY =>
        HttpStatus.SERVICE_UNAVAILABLE.value()

      case Errors.RECORD_NOT_FOUND | Errors.FILE_NOT_FOUND =>
        HttpStatus.NOT_FOUND.value()

      case Errors.UNSUPPORTED_TYPE =>
        HttpStatus.NOT_IMPLEMENTED.value()

      case Errors.RESOURCE_OCCUPIED | Errors.TOO_MANY_REQUEST =>
        HttpStatus.TOO_MANY_REQUESTS.value()

      case Errors.TIMEOUT =>
        HttpStatus.REQUEST_TIMEOUT.value()

      case _ =>
        HttpStatus.BAD_REQUEST.value()
    }
  }
}
