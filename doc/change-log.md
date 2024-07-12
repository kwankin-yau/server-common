# 3.1.0 - [2024-01-21]
## Improvement
- 令 `NettyEnv` 实现 `AutoCloseable` 接口
- 新增 `com.lucendar.common.serv.cleanup.DbCleaner` 接口
- 新增 `com.lucendar.common.serv.cleanup.DbCleanupReq`
- `PeriodicalCleanupConfig` 新增 `earliestKeepTimeFromNow` 方法
- `SslConfig` 增加 PEM 证书支持
- 新增 `com.lucendar.common.serv.utils.SslUtils`

# 3.0.0 - [2023-11-28]
## Improvement
- Introduce NetUtils2 which retains dns function original exists in NetUtils
- Introduce `com.lucendar.common.serv.servlet.ServletHelper.setErrorResp`
- Introduce `com.lucendar.common.serv.utils.ServUtils.mapMimeTypeFromFileName`
- Introduce `com.lucendar.common.serv.utils.ServUtils.errCodeToHttpStatusCode`
- Introduce `com.lucendar.common.serv.utils.NettyEnv`

## Changed
- Maven group changed `com.lucendar`
- Move the following classes to lucendar-common.
  - NetUtils (some class moved to NetUtils2)

- Dependencies:
  - Bump `lucendar-common` to 2.0.0

# 2.0.0 - [2023-09-07]
## Changed
- Introduce `lucendar-common`
- Bump `gratour-common` to 3.3.0

# 1.1.0 - [2023-05-02]
## Changed
- Dependencies:
  - require a minimum of JDK 17 at runtime
  - as well as a minimum of Tomcat 10 / Jetty 11 (for Jakarta EE 9 compatibility)

# 1.0.6 - [2023-01-12]
## Improvement
- `ConfigLoader` now check system property `spring.config.location` for override config directory.

# 1.0.5 - [2022-12-11]
## Changed
- Dependencies:
  - Bump `scala-library` from `2.13.6` to `2.13.10`
  - Bump `gratour-common` from `3.2.4` to `3.2.7`

# 1.0.4 - [2022-08-03]
## Improvement
- Introduce ServletHelper.addCorsHeaders(HttpServletRequest, HeadersBuilder[_])

# 1.0.3 - [2022-01-03]
## Improvement
- Introduce `com.lucendar.common.serv.utils.ByteBufHelper`
- Introduce `com.lucendar.common.serv.utils.ConfigLoader`

## Changed
- Dependencies:
  - Bump `spring` from `5.3.9` to `5.3.14`
  - Bump `spring-security-core` from `5.5.2` to `5.5.4`
  - Bump `gratour-common` from `3.2.2` to `3.2.4`
  - Bump `scala-logging` from `3.9.2` to `3.9.4`
  - Add `io.netty:netty-buffer`

# 1.0.2 - [2021-11-15]
## Improvement
- `ServletHelper` added `addCorsHeaders(headerBuilder: HeadersBuilder[_]): Unit`
- `PeriodicalCleanupConfig`, `QuotaCleanupConfig` implement `Clonable` interface

# 1.0.1 - [2021-10-20]
## Improvement
- Both `CleanupByPeriod` and `CleanupByQuota` logic add a name for give more detailed log info.
- Added `AuthInterceptor`, `DefaultCorsFilter`, `ExceptionAdvisor`, `UserSessionResolver`, `ServUtils` unit.

## Changed
- PeriodicalCleanupConfig
  - add `keepDays`
  - rename `keepDataDurationMinutes` property to `keepMinutes`
