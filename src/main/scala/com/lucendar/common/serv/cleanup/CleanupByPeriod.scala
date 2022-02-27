package com.lucendar.common.serv.cleanup


import com.typesafe.scalalogging.Logger

import java.time.OffsetDateTime
import scala.reflect.ClassTag

trait CleanupByPeriod[ITEM] {

  def logger: Logger

  def name: String

  val config: PeriodicalCleanupConfig
  val defaultKeepMinutes: Int

  /**
   * Query earliest records which timestamp is before given `timestamp`.
   *
   * @param timeBefore timestamp used to determent which records should be delete.
   * @return should be deleted records.
   */
  def qryEarliestItems(timeBefore: Long): java.util.List[ITEM]

  /**
   * Delete records
   *
   * @param timeBefore delete records which timestamp is before given `timeBefore`
   */
  def deleteItems(timeBefore: Long): Unit

  def execute()(implicit classTag: ClassTag[ITEM]): java.util.List[ITEM] = {
    logger.debug(s"[$name] Starting execute cleanup by time: " + classTag.runtimeClass.getSimpleName)

    var time = OffsetDateTime.now()

    val minutes = config.keepMinutesDef(defaultKeepMinutes)
    time = time.minusMinutes(minutes)
    logger.debug(s"[${name}] Delete records before time: " + time + s", keep data in minutes: ${minutes}")

    val timeBefore = time.toInstant.toEpochMilli

    val r = qryEarliestItems(timeBefore)
    deleteItems(timeBefore)
    logger.debug("Cleanup by time: " + classTag.runtimeClass.getSimpleName + ", deleted " + r.size() + " records.")
    r
  }
}

