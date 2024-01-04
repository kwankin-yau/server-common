/**   *****************************************************************************
 * Copyright (c) 2019, 2022 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 *  *****************************************************************************   */
package com.lucendar.common.serv

import com.lucendar.common.utils.{BcdUtils, StringUtils}
import io.netty.buffer.ByteBuf
import org.apache.commons.codec.binary.Hex

import java.nio.charset.Charset
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, TimeUnit}
import scala.util.matching.Regex

package object utils {

  implicit class ByteBufHelper(byteBuf: ByteBuf) {

    def convertToByteArray: Array[Byte] = {
      if (byteBuf.hasArray)
        byteBuf.array()
      else {
        val readerIndex = byteBuf.readerIndex()
        byteBuf.readerIndex(0)
        val r = new Array[Byte](byteBuf.readableBytes())
        byteBuf.readBytes(r)
        byteBuf.readerIndex(readerIndex)
        r
      }
    }

    def remainingToHexString: String = {
      val size = byteBuf.readableBytes()
      if (size > 0) {
        val bytes = new Array[Byte](size)
        val readerIndex = byteBuf.readerIndex()
        byteBuf.readBytes(bytes)
        byteBuf.readerIndex(readerIndex)
        Hex.encodeHexString(bytes)
      } else
        ""
    }

    def readBytesLen(len: Int): Array[Byte] = {
      val r = new Array[Byte](len)
      byteBuf.readBytes(r)
      r
    }

    def readBytesHex(len: Int): String = Hex.encodeHexString(readBytesLen(len))

    def readStr(len: Int, charset: Charset): String = {
      val bytes = new Array[Byte](len)
      byteBuf.readBytes(bytes)
      new String(bytes, charset)
    }

    def readCStr(maxLen: Int, charset: Charset): String = {
      val bytes = new Array[Byte](maxLen)
      byteBuf.readBytes(bytes)
      val l = StringUtils.strLen(bytes)
      new String(bytes, 0, l, charset)
    }

    def readStrMaxLen(maxLen: Int, charset: Charset): String = {
      val bytes = new Array[Byte](maxLen)
      byteBuf.readBytes(bytes)

      StringUtils.strMaxLen(bytes, 0, maxLen, charset)
    }

    def readRemainingAsStr(charset: Charset): String = {
      val size = byteBuf.readableBytes()
      if (size > 0)
        readStr(size, charset)
      else
        ""
    }

    private val EMPTY_BYTE_ARRAY: Array[Byte] = new Array[Byte](0)

    def readRemainingAsBytes(): Array[Byte] = {
      val size = byteBuf.readableBytes()
      if (size > 0) {
        val r = new Array[Byte](size)
        byteBuf.readBytes(r)
        r
      }
      else
        EMPTY_BYTE_ARRAY
    }

    def readByteLenPrefixedStr(charset: Charset): String = {
      val size = byteBuf.readUnsignedByte()
      readStr(size, charset)
    }

    def readShortLenPrefixedStr(charset: Charset): String = {
      val size = byteBuf.readUnsignedShort()
      readStr(size, charset)
    }

    def readIntLenPrefixedStr(charset: Charset): String = {
      val size = byteBuf.readInt()
      readStr(size, charset)
    }

    def readBcd(bytes: Int): String = {
      val buf = new Array[Byte](bytes)
      byteBuf.readBytes(buf)
      BcdUtils.decode(buf)
    }

    def writeStr(s: String, charset: Charset): Unit = {
      if (s != null) {
        val bytes = s.getBytes(charset)
        byteBuf.writeBytes(bytes)
      }
    }

    def writeFixedLenStr(s: String, len: Int, charset: Charset): Unit = {
      if (s == null || s.isEmpty) {
        byteBuf.writeZero(len)
        return
      }

      val bytes = s.getBytes(charset)
      val l = bytes.length
      if (l > len) {
        throw new RuntimeException(s"String `$s` is too long(limit=$len).")
      }

      byteBuf.writeBytes(bytes)
      val delta = len - l
      if (delta > 0)
        byteBuf.writeZero(delta)
    }

    def writeFixedLenBytes(bytes: Array[Byte], len: Int): Unit = {
      if (bytes == null || bytes.isEmpty) {
        byteBuf.writeZero(len)
        return
      }

      val l = bytes.length
      if (l > len) {
        throw new RuntimeException(s"Bytes `$bytes` is too long(limit=$len).")
      }

      byteBuf.writeBytes(bytes)
      val delta = len - l
      if (delta > 0)
        byteBuf.writeZero(delta)
    }

    def writeFixedLenBytesHex(hex: String, len: Int): Unit = {
      val bytes = Hex.decodeHex(hex)
      writeFixedLenBytes(bytes, len)
    }

    def writeByteLenPrefixedStr(s: String, charset: Charset): Unit = {
      if (s == null || s.isEmpty) {
        byteBuf.writeByte(0)
        return
      }

      val bytes = s.getBytes(charset)
      byteBuf.writeByte(bytes.length)
      byteBuf.writeBytes(bytes)
    }

    def writeShortLenPrefixedStr(s: String, charset: Charset): Unit = {
      if (s == null || s.isEmpty) {
        byteBuf.writeShort(0)
        return
      }

      val bytes = s.getBytes(charset)
      byteBuf.writeShort(bytes.length)
      byteBuf.writeBytes(bytes)
    }

    def writeIntLenPrefixedStr(s: String, charset: Charset): Unit = {
      if (s == null || s.isEmpty) {
        byteBuf.writeInt(0)
        return
      }

      val bytes = s.getBytes(charset)
      byteBuf.writeInt(bytes.length)
      byteBuf.writeBytes(bytes)
    }

    def writeByteLenPrefixedBytes(bytes: Array[Byte]): Unit = {
      if (bytes == null || bytes.isEmpty) {
        byteBuf.writeByte(0)
        return
      }

      byteBuf.writeByte(bytes.length)
      byteBuf.writeBytes(bytes)
    }

    def writeShortLenPrefixedBytes(bytes: Array[Byte]): Unit = {
      if (bytes == null || bytes.isEmpty) {
        byteBuf.writeShort(0)
        return
      }

      byteBuf.writeShort(bytes.length)
      byteBuf.writeBytes(bytes)
    }

    def writeIntLenPrefixedBytes(bytes: Array[Byte]): Unit = {
      if (bytes == null || bytes.isEmpty) {
        byteBuf.writeInt(0)
        return
      }

      byteBuf.writeInt(bytes.length)
      byteBuf.writeBytes(bytes)
    }

    def writeBcd(s: String): Unit = {
      val bytes = BcdUtils.encode(s)
      byteBuf.writeBytes(bytes)
    }

    /**
     * Short name for byteBufToHexStringKeepReaderIndex
     *
     * @param buf
     * @return
     */
    def bufToHex(keepReaderIndex: Boolean): String = if (keepReaderIndex) {
      val readerIndex = byteBuf.readerIndex
      val size = byteBuf.readableBytes
      val bytes = new Array[Byte](size)
      byteBuf.readBytes(bytes)
      byteBuf.readerIndex(readerIndex)
      Hex.encodeHexString(bytes)
    }
    else {
      val size = byteBuf.readableBytes
      val bytes = new Array[Byte](size)
      byteBuf.readBytes(bytes)
      Hex.encodeHexString(bytes)
    }

    def bufToHex(): String = bufToHex(true)

    def toByteArray(): Array[Byte] = {
      if (byteBuf.hasArray)
        byteBuf.array
      else {
        val r = new Array[Byte](byteBuf.readableBytes)
        byteBuf.readBytes(r)
        r
      }
    }

  }

  implicit class PathExtractor(sc: StringContext) {

    object path {
      def unapplySeq(str: String): Option[Seq[String]] =
        sc.parts.map(Regex.quote).mkString("^", "([^/]+)", "$").r.unapplySeq(str)
   }

  }

  implicit class CollectionHelper[+T](collection: java.util.Collection[T]) {
    def find(p: T => Boolean): Option[T] = {
      val iter = collection.iterator()
      while (iter.hasNext) {
        val t = iter.next()
        if (p(t))
          return Some(t)
      }

      None
    }

    def exists(p: T => Boolean): Boolean = {
      val iter = collection.iterator()
      while (iter.hasNext) {
        val t = iter.next()
        if (p(t))
          return true
      }

      false
    }

    def removeWhere(p: T => Boolean): T = {
      val iter = collection.iterator()
      while (iter.hasNext) {
        val t = iter.next()
        if (p(t)) {
          iter.remove()
          return t
        }
      }

      null.asInstanceOf[T]
    }

    def nonEmpty: Boolean = !collection.isEmpty
  }

  implicit class ListHelper[+T <: AnyRef](list: java.util.List[T]) {

    def indexWhere(p: T => Boolean): Int = {
      for (i <- 0 until list.size()) {
        val item = list.get(i)
        if (p(item))
          return i
      }

      -1
    }

    def nonEmpty: Boolean = !list.isEmpty

    def first: Option[T] = if (list.isEmpty) None else Some(list.get(0))

    def last: Option[T] = if (list.isEmpty) None else Some(list.get(list.size() - 1))
  }

  def convertFuture[T](x: Future[T]): java.util.concurrent.Future[T] = {
    new java.util.concurrent.Future[T] {
      override def isCancelled: Boolean = throw new UnsupportedOperationException

      override def get(): T = Await.result(x, Duration.Inf)

      override def get(timeout: Long, unit: TimeUnit): T = Await.result(x, Duration.create(timeout, unit))

      override def cancel(mayInterruptIfRunning: Boolean): Boolean = throw new UnsupportedOperationException

      override def isDone: Boolean = x.isCompleted
    }
  }



}
