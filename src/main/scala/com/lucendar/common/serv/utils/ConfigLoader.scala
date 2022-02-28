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
import info.gratour.common.utils.JsonUtils
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ResourceLoader

import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import scala.util.Using

class ConfigLoader(val resourceLoader: ResourceLoader, val activeProfile: String) {

  import ConfigLoader.logger


  private def loadStringFromRes(fileName: String, resourceLoader: ResourceLoader): String = {
    var res = resourceLoader.getResource("file:config/" + fileName)
    if (!res.exists()) {
      res = resourceLoader.getResource("classpath:" + fileName)
      if (!res.exists())
        return null
    }

    Using.resource(res.getInputStream) { in =>
      IOUtils.toString(in, StandardCharsets.UTF_8)
    }
  }

  def loadText[T >: Null](baseName: String, fileExt: String, convert: String => T): Option[T] = {
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

  def loadJson[T >: Null](baseName: String, typ: Type): Option[T] = {
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

}

object ConfigLoader {
  private val logger = Logger("com.lucendar.configLoader")
}
