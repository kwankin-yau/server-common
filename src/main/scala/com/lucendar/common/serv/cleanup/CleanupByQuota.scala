package com.lucendar.common.serv.cleanup



import com.lucendar.common.serv.cleanup.CleanupByQuota.CleanupByQuotaResult

import java.util


trait CleanupByQuota[ITEM] {

  val config: QuotaConfig

  /**
   * The delete target average file size in bytes. Used to estimate item record should be delete.
   */
  val avgTargetItemSize: Long

  /**
   * Get file size of the target item.
   *
   * @param item target item
   * @return file size of the target item.
   */
  def fileSizeOfItem(item: ITEM): Long

  /**
   * Query earliest target items in processing table.
   *
   * @param limit the count of earliest target
   * @return earliest target items in processing table.
   */
  def qryEarliestItems(limit: Int): java.util.List[ITEM]

  /**
   * Delete a batch records.
   *
   * @param sizeToDelete      total file size expected to delete
   * @param itemCount         estimated target item count
   * @param filesNeedToDelete files need to delete, used to return deleted items.
   * @return delete result.
   */
  def deleteBatch(sizeToDelete: Long, itemCount: Int, filesNeedToDelete: java.util.List[ITEM]): CleanupByQuotaResult = {
    val items = qryEarliestItems(itemCount)
    var deletedSize: Long = 0
    var isAllDeleted: Boolean = false
    var sizeRemains = sizeToDelete

    if (!items.isEmpty) {
      isAllDeleted = items.size() < itemCount

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

    CleanupByQuotaResult(deletedSize, isAllDeleted)
  }

  /**
   * Query the total file size used in all items in table.
   *
   * @return total space usage in bytes.
   */
  def qryTotalSize(): Long

  /**
   * Update the total size of files used in all items in table. Called when files corresponding to items deleted.
   *
   * @param deletedSize size of deleted files.
   * @return new total size of files used in all items in table.
   */
  def updateTotalSize(deletedSize: Long): Long

  /**
   * Execute quota based deletion.
   *
   * @return files need to delete.
   */
  def execute(): java.util.List[ITEM] = {
    val r = new util.ArrayList[ITEM]()
    val totalSize = qryTotalSize()
    if (totalSize > config.quota()) {
      var sizeToDelete = config.deleteBatchSize()
      var isAllDeleted = false

      while (sizeToDelete > 0 && !isAllDeleted) {
        var itemCount = sizeToDelete / avgTargetItemSize
        if (itemCount == 0 || itemCount * avgTargetItemSize < sizeToDelete)
          itemCount += 1

        val result = deleteBatch(sizeToDelete, itemCount.toInt, r)
        updateTotalSize(result.deletedSize)

        sizeToDelete -= result.deletedSize
        isAllDeleted = result.isAllDeleted
      }
    }

    r
  }
}

object CleanupByQuota {
  case class CleanupByQuotaResult(deletedSize: Long, isAllDeleted: Boolean)
}
