/*******************************************************************************
 *  Copyright (c) 2019, 2021 lucendar.com.
 *  All rights reserved.
 *
 *  Contributors:
 *     KwanKin Yau (alphax@vip.163.com) - initial API and implementation
 *******************************************************************************/
package com.lucendar.common.serv.cleanup;

import info.gratour.common.types.validate.ValidateResultReceiver;
import org.springframework.scheduling.support.CronExpression;

import java.util.function.Consumer;

public class PeriodicalCleanupConfig implements CleanupConfig {

    public static int KEEP_ONE_MONTH = 32;
    public static int KEEP_HALF_YEAR = 183;

    private boolean enabled;
    private String cron;
    private int keepDays;
    private long keepMinutes = 2880; // only for debug

    public PeriodicalCleanupConfig() {
    }

    public PeriodicalCleanupConfig(boolean enabled, String cron, long keepDataDurationMinutes) {
        this.enabled = enabled;
        this.cron = cron;
        this.keepMinutes = keepDataDurationMinutes;
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

    public int getKeepDays() {
        return keepDays;
    }

    public void setKeepDays(int keepDays) {
        this.keepDays = keepDays;
    }

    public long getKeepMinutes() {
        return keepMinutes;
    }

    public void setKeepMinutes(long keepMinutes) {
        this.keepMinutes = keepMinutes;
    }

    public void validate(ValidateResultReceiver receiver) {
        if (enabled) {
            if (cron == null || cron.isEmpty() || !CronExpression.isValidExpression(cron))
                receiver.invalidField("cron");

            if (keepDays < 0)
                receiver.invalidField("keepDays");

            if (keepDays == 0 && keepMinutes < 0)
                receiver.invalidField("keepDays");
        }
    }

    @Override
    public String toString() {
        return "PeriodicalCleanupConfig{" +
                "enabled=" + enabled +
                ", cron='" + cron + '\'' +
                ", keepDays=" + keepDays +
                ", keepMinutes=" + keepMinutes +
                '}';
    }
}
