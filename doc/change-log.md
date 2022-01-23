# 1.0.3 - [2022-01-03]
## Improvement
- Introduce `com.lucendar.common.serv.utils.ByteBufHelper`

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
