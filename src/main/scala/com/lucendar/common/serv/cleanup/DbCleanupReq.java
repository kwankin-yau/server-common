/*
 * Copyright (c) 2024 lucendar.com.
 * All rights reserved.
 */
package com.lucendar.common.serv.cleanup;

import javax.sql.DataSource;
import java.util.StringJoiner;

/**
 * 历史数据清理请求
 *
 * @param <T> 请求类型
 * @param <C> 数据清理配置类型
 */
public class DbCleanupReq<T, C extends CleanupConfig> {

    private final DataSource ds;
    private final C config;
    private final T req;

    /**
     * 构造函数
     *
     * @param ds 数据源
     * @param config 数据清理配置
     * @param req 数据清理请求
     */
    public DbCleanupReq(DataSource ds, C config, T req) {
        this.ds = ds;
        this.config = config;
        this.req = req;
    }

    /**
     * 取数据源
     *
     * @return 数据源
     */
    public DataSource getDs() {
        return ds;
    }

    /**
     * 取清理配置
     *
     * @return 清理配置
     */
    public C getConfig() {
        return config;
    }

    /**
     * 取原始请求对象
     *
     * @return 原始请求对象
     */
    public T getReq() {
        return req;
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", DbCleanupReq.class.getSimpleName() + "[", "]")
                .add("ds=" + ds)
                .add("config=" + config)
                .add("req=" + req)
                .toString();
    }
}
