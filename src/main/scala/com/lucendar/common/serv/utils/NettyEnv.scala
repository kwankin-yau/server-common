/*
 * Copyright (c) 2024 lucendar.com.
 * All rights reserved.
 */
package com.lucendar.common.serv.utils

import io.netty.channel.nio.NioEventLoopGroup

/**
 * Netty environment.
 *
 * @param bossGroup
 * @param workerGroup
 */
case class NettyEnv(bossGroup: NioEventLoopGroup, workerGroup: NioEventLoopGroup) extends AutoCloseable {
  override def close(): Unit = {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
