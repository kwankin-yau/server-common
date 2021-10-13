/*******************************************************************************
 *  Copyright (c) 2019, 2021 lucendar.com.
 *  All rights reserved.
 *
 *  Contributors:
 *     KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 *******************************************************************************/
package com.lucendar.common.serv.cleanup;

public class PeriodicalCleanupConfig implements CleanupConfig {

    public static int KEEP_ONE_MONTH = 31;
    public static int KEEP_HALF_YEAR = 183;

    private boolean enabled;
    private String cron;
    private long keepDataDurationMinutes;

    public PeriodicalCleanupConfig() {
    }

    public PeriodicalCleanupConfig(boolean enabled, String cron, long keepDataDurationMinutes) {
        this.enabled = enabled;
        this.cron = cron;
        this.keepDataDurationMinutes = keepDataDurationMinutes;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public long getKeepDataDurationMinutes() {
        return keepDataDurationMinutes;
    }

    public void setKeepDataDurationMinutes(long keepDataDurationMinutes) {
        this.keepDataDurationMinutes = keepDataDurationMinutes;
    }

    @Override
    public String toString() {
        return "PeriodicalCleanupConfig{" +
                "enabled=" + enabled +
                ", cron='" + cron + '\'' +
                ", keepDataDurationMinutes=" + keepDataDurationMinutes +
                '}';
    }
}
