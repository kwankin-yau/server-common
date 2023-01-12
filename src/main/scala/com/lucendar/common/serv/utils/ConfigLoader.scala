/** *****************************************************************************
 * Copyright (c) 2019, 2022 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 * ***************************************************************************** */
package com.lucendar.common.serv.utils

import com.google.gson.{JsonObject, JsonParser}
import com.typesafe.scalalogging.Logger
import info.gratour.common.Consts
import info.gratour.common.error.{ErrorWithCode, Errors}
import info.gratour.common.utils.JsonUtils
import org.apache.commons.io.{FilenameUtils, IOUtils}
import org.springframework.core.io.{Resource, ResourceLoader}

import java.io.File
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import scala.util.Using

class ConfigLoader(val resourceLoader: ResourceLoader, val activeProfile: String) {

  import ConfigLoader.logger

  private val springConfigLocation: String = {
    var location = System.getProperty("spring.config.location")
    if (location != null) {
      val f = new File(location)
      if (!f.exists() || !f.isDirectory)
        location = null
    }

    location
  }

  private def loadStringFromRes(res: Resource): String = {
    Using.resource(res.getInputStream) { in =>
      IOUtils.toString(in, StandardCharsets.UTF_8)
    }
  }


  private def loadStringFromRes(fileName: String, resourceLoader: ResourceLoader): String = {
    var res: Resource = null
    if (springConfigLocation != null) {
      val fn = FilenameUtils.concat(springConfigLocation, fileName)
      res = resourceLoader.getResource("file:" + fn)
      if (res.exists())
        return loadStringFromRes(res)
    }

    res = resourceLoader.getResource("file:config/" + fileName)
    if (!res.exists()) {
      res = resourceLoader.getResource("classpath:" + fileName)
      if (!res.exists())
        return null
    }

    loadStringFromRes(res)
  }

  def loadTextOpt[T >: Null](baseName: String, fileExt: String, convert: String => T): Option[T] = {
    val primaryConfig =
      if (activeProfile != null && activeProfile.nonEmpty) {
        s"$baseName-$activeProfile.$fileExt"
      } else
        s"$baseName.$fileExt"

    logger.info(s"Loading `$primaryConfig`.")

    val s = loadStringFromRes(primaryConfig, resourceLoader)
    if (s == null)
      None
    else
      Some(convert(s))
  }

  private def notFoundException(baseName: String, fileExt: String): ErrorWithCode = {
    val message =
      if (activeProfile != null)
        s"Configuration file: `$baseName.$fileExt` does not found under profile: $activeProfile."
      else
        s"Configuration file: `$baseName.$fileExt` does not found."

    new ErrorWithCode(Errors.INVALID_CONFIG, message)
  }

  def loadText[T >: Null](baseName: String, fileExt: String, convert: String => T): T = {
    val r = loadTextOpt(baseName, fileExt, convert)
    if (r.isEmpty)
      throw notFoundException(baseName, fileExt)

    r.get
  }

  def loadJsonOpt[T >: Null](baseName: String, typ: Type): Option[T] = {
    val (primaryConfig, overrideConfig) =
      if (activeProfile != null && activeProfile.nonEmpty) {
        (s"$baseName-$activeProfile.json", s"$baseName-override-$activeProfile.json")
      } else
        (s"$baseName.json", s"$baseName-override.json")

    logger.info(s"Loading `$primaryConfig`.")
    var s = loadStringFromRes(primaryConfig, resourceLoader)
    if (s == null)
      return None

    val json: JsonObject = JsonParser.parseString(s).getAsJsonObject


    s = loadStringFromRes(overrideConfig, resourceLoader)
    if (s != null) {
      logger.info(s"Loading `$overrideConfig`.")
      val json2: JsonObject = JsonParser.parseString(s).getAsJsonObject
      JsonUtils.deepMerge(json, json2)
    } else
      logger.info(s"`${overrideConfig}` NOT found, ignored.")

    Some(Consts.GSON.fromJson(json, typ))
  }

  def loadJson[T >: Null](baseName: String, typ: Type): T = {
    val r = loadJsonOpt(baseName, typ)
    if (r.isEmpty)
      throw notFoundException(baseName, "json")

    r.get
  }

}

object ConfigLoader {
  private val logger = Logger("com.lucendar.configLoader")
}
