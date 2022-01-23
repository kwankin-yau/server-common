/**   *****************************************************************************
 * Copyright (c) 2019, 2022 lucendar.com.
 * All rights reserved.
 *
 * Contributors:
 * KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 *  *****************************************************************************   */
package com.lucendar.common.serv

import info.gratour.common.utils.{BcdUtils, StringUtils}
import io.netty.buffer.ByteBuf
import org.apache.commons.codec.binary.Hex

import java.nio.charset.Charset

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

}
