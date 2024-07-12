/*******************************************************************************
 *  Copyright (c) 2019, 2021 lucendar.com.
 *  All rights reserved.
 *
 *  Contributors:
 *     KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 *******************************************************************************/
package com.lucendar.common.serv.cleanup;

import com.lucendar.common.types.ValidateResultReceiver;
import info.gratour.common.error.ErrorWithCode;
import org.springframework.scheduling.support.CronExpression;

import java.time.Instant;
import java.util.StringJoiner;

public class PeriodicalCleanupConfig implements CleanupConfig, Cloneable {

    public static int KEEP_DAYS_ONE_WEEK = 8; // 7 + 1
    public static int KEEP_DAYS_ONE_MONTH = 32; // 31 + 1
    public static int KEEP_DAYS_HALF_YEAR = 183; // 183

    public static int KEEP_MINUTES_ONE_WEEK = KEEP_DAYS_ONE_WEEK * 24 * 60;
    public static int KEEP_MINUTES_ONE_MONTH = KEEP_DAYS_ONE_MONTH * 24 * 60;
    public static int KEEP_MINUTES_HALF_YEAR = KEEP_DAYS_HALF_YEAR * 24 * 60;


    private boolean enabled;
    private String cron;
    private Integer keepDays;
    private Integer keepMinutes; // only for debug

    public PeriodicalCleanupConfig() {
    }

    public PeriodicalCleanupConfig(boolean enabled, String cron, Integer keepDays, Integer keepMinutes) {
        this.enabled = enabled;
        this.cron = cron;
        this.keepDays = keepDays;
        this.keepMinutes = keepMinutes;
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

    public Integer getKeepDays() {
        return keepDays;
    }

    public void setKeepDays(Integer keepDays) {
        this.keepDays = keepDays;
    }

    public Integer getKeepMinutes() {
        return keepMinutes;
    }

    public void setKeepMinutes(Integer keepMinutes) {
        this.keepMinutes = keepMinutes;
    }

    public int keepMinutesDef(int defaultValue) {
        if (keepMinutes != null) {
            return keepMinutes;
        } else if (keepDays != null)
            return keepDays * 24 * 60;
        else
            return defaultValue;
    }

    public long keepSecondsDef(long defaultValue) {
        if (keepMinutes != null) {
            return keepMinutes * 60;
        } else if (keepDays != null)
            return keepDays * 24 * 60 * 60;
        else
            return defaultValue;
    }

    public long earliestKeepTimeFromNow() {
        if (keepMinutes == null && keepDays == null)
            throw ErrorWithCode.invalidConfig("keepDays");

        long seconds = keepSecondsDef(0L);
        return Instant.now().minusSeconds(seconds).toEpochMilli();
    }

    public void validate(ValidateResultReceiver receiver) {
        if (enabled) {
            if (cron == null || cron.isEmpty() || !CronExpression.isValidExpression(cron))
                receiver.invalidField("cron");

            if (keepDays != null && keepDays < 0)
                receiver.invalidField("keepDays");

            if (keepMinutes != null && keepMinutes < 0)
                receiver.invalidField("keepMinutes");
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PeriodicalCleanupConfig.class.getSimpleName() + "[", "]")
                .add("enabled=" + enabled)
                .add("cron='" + cron + "'")
                .add("keepDays=" + keepDays)
                .add("keepMinutes=" + keepMinutes)
                .toString();
    }

    @Override
    public PeriodicalCleanupConfig clone() {
        try {
            return (PeriodicalCleanupConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
