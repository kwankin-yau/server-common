package com.lucendar.common.serv.cleanup


import java.time.{Duration, OffsetDateTime}

trait Cleanup[ITEM] {

  val config: CleanupConfig

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

  def execute(): java.util.List[ITEM] = {
    val time = OffsetDateTime.now()
    val timeBefore = time.minus(config.keepDataDuration).toInstant.toEpochMilli
    val r = qryEarliestItems(timeBefore)
    deleteItems(timeBefore)
    r
  }
}

case class CleanupConfig(keepDataDuration: Duration)

object CleanupConfig {
  val KEEP_HALF_YEAR_CLEANUP_CONFIG: CleanupConfig = CleanupConfig(Duration.ofDays(183))
}
