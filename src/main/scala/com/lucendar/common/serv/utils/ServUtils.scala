/** *****************************************************************************
 * Copyright (c) 2019, 2021 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 * ***************************************************************************** */
package com.lucendar.common.serv.utils

import org.apache.commons.codec.binary.Base64
import org.springframework.core.io.ResourceLoader

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

}
