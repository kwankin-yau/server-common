package com.lucendar.common.serv.cleanup


import com.lucendar.common.serv.cleanup.CleanupByQuota.CleanupByQuotaResult
import com.typesafe.scalalogging.Logger

import java.util
import java.util.concurrent.atomic.AtomicLong
import scala.reflect.ClassTag


trait CleanupByQuota[ITEM] {

  def logger: Logger

  val config: QuotaCleanupConfig

  /**
   * The delete target average file size in bytes. Used to estimate item record should be delete.
   */
  val avgTargetItemSize: Long

  def cachedTotalDiskUsage: AtomicLong

  protected var totalDiskUsageLoaded: Boolean = false
  protected var executeTimes: Long = 0

  /**
   * Get file size of the target item.
   *
   * @param item target item
   * @return file size of the target item.
   */
  def fileSizeOfItem(item: ITEM): Long

  /**
   * Query earliest target items in target table.
   *
   * @param limit the count of earliest target
   * @return earliest target items in processing table.
   */
  def qryEarliestItems(limit: Int): java.util.List[ITEM]

  /**
   * Delete earliest n target items in target table.
   *
   * @param n the count of earliest target
   */
  def deleteEarliestItems(n: Int): Unit

  /**
   * Delete a batch records.
   *
   * @param sizeToDelete      total file size expected to delete
   * @param itemCount         estimated target item count
   * @param filesNeedToDelete files need to delete, used to return deleted items.
   * @return delete result.
   */
  def deleteBatch(sizeToDelete: Long, itemCount: Int, filesNeedToDelete: java.util.List[ITEM])(implicit classTag: ClassTag[ITEM]): CleanupByQuotaResult = {
    logger.debug(s"deleteBatch(${classTag.runtimeClass.getSimpleName}): sizeToDelete=$sizeToDelete, itemCount=$itemCount")
    val items = qryEarliestItems(itemCount)
    var deletedSize: Long = 0
    var isAllDeleted: Boolean = false
    var sizeRemains = sizeToDelete

    if (!items.isEmpty) {
      isAllDeleted = items.size() < itemCount

      deleteEarliestItems(items.size())
      logger.debug(s"delete ${items.size()} earliest items.")

      import scala.util.control.Breaks._
      breakable {
        for (i <- 0 until items.size()) {
          val item = items.get(i)
          val fileSize = fileSizeOfItem(item)
          sizeRemains -= fileSize
          deletedSize += fileSize

          filesNeedToDelete.add(item)

          if (sizeRemains <= 0) {
            break()
          }
        }
      }
    }

    logger.debug(s"deleteBatch(${classTag.runtimeClass.getSimpleName}): deletedSize=$deletedSize, isAllDeleted=$isAllDeleted")
    CleanupByQuotaResult(deletedSize, isAllDeleted)
  }

  /**
   * Query the total file size used in all items in table.
   *
   * @return total space usage in bytes.
   */
  def qryTotalSize(): Long

  protected def updateCachedTotalDiskUsage(): Unit = {
    val total = qryTotalSize()
    logger.debug("qryTotalSize returns " + total)
    cachedTotalDiskUsage.set(total)
    totalDiskUsageLoaded = true
  }

  /**
   * Execute quota based deletion.
   *
   * @return files need to delete.
   */
  def execute()(implicit classTag: ClassTag[ITEM]): java.util.List[ITEM] = {
    logger.debug("Starting cleanup by quota: " + classTag.runtimeClass.getSimpleName + ", using config: " + config + ".")

    var loadTotal = !totalDiskUsageLoaded
    if (!loadTotal) {
      if ((executeTimes & 7) == 7)
        loadTotal = true
    }

    if (loadTotal)
      updateCachedTotalDiskUsage()

    val r = new util.ArrayList[ITEM]()
    val totalSize = qryTotalSize()
    logger.debug(s"qryTotalSize(${classTag.runtimeClass.getSimpleName}) returned " + totalSize)
    var reQuery = false

    if (totalSize > config.quota()) {
      var sizeToDelete = config.deleteBatchSize()
      var isAllDeleted = false

      while (sizeToDelete > 0 && !isAllDeleted) {
        var itemCount = sizeToDelete / avgTargetItemSize
        if (itemCount == 0 || itemCount * avgTargetItemSize < sizeToDelete)
          itemCount += 1

        val result = deleteBatch(sizeToDelete, itemCount.toInt, r)
        val newTotal = cachedTotalDiskUsage.addAndGet(-result.deletedSize)
        logger.debug(s"UpdateTotalSize(-${result.deletedSize}) returned new total is: " + newTotal)
        if (newTotal < 0)
          reQuery = true

        sizeToDelete -= result.deletedSize
        isAllDeleted = result.isAllDeleted
      }
    }

    executeTimes += 1
    if (reQuery)
      updateCachedTotalDiskUsage()

    logger.debug("Cleanup by quota: " + classTag.runtimeClass.getSimpleName + ", delete " + r.size() + " records.")

    r
  }
}

object CleanupByQuota {
  case class CleanupByQuotaResult(deletedSize: Long, isAllDeleted: Boolean)
}
