/*
 * Copyright (c) 2024 lucendar.com.
 * All rights reserved.
 */
package com.lucendar.common.serv.cleanup;

import java.sql.Connection;

/**
 * 历史数据清理
 *
 * @param <C> 配置类型
 * @param <T> 原始请求类型
 */
public interface DbCleaner<C extends CleanupConfig, T> {

    /**
     * 执行历史数据清理
     *
     * @param conn 数据库连接
     * @param config 历史数据清理配置
     * @param req 原始请求对象
     */
    void exec(Connection conn, C config, T req);
}
