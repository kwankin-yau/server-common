package com.lucendar.common.serv.db;

public class DbUtils {

    public static final String SQL_DIALECT__POSTGRESQL = "postgresql";
    public static final String SQL_DIALECT__MYSQL = "mysql";
    public static final String SQL_DIALECT__ORACLE = "oracle";
    public static final String SQL_DIALECT__SQLSERVER = "sqlserver";
    public static final String SQL_DIALECT__SQLITE = "sqlite";
    public static final String SQL_DIALECT__H2 = "h2";

    /**
     *
     * @param jdbcUrl
     * @return the sql dialect, null if parse failed
     */
    public static String parseSqlDialectFromJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null)
            throw new IllegalArgumentException("jdbcUrl(null)");

        if (!jdbcUrl.startsWith("jdbc:"))
            return null;

        var startIndex = "jdbc:".length();
        var endIndex = jdbcUrl.indexOf(':', startIndex);

        return jdbcUrl.substring(startIndex, endIndex);
    }

}
