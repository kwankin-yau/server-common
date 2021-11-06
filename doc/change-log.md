# 1.0.1 - [2021-10-20]
## Improvement
- Both `CleanupByPeriod` and `CleanupByQuota` logic add a name for give more detailed log info.
- Added `AuthInterceptor`, `DefaultCorsFilter`, `ExceptionAdvisor`, `UserSessionResolver`, `ServUtils` unit.

## Changed
- PeriodicalCleanupConfig
  - add `keepDays`
  - rename `keepDataDurationMinutes` property to `keepMinutes`
