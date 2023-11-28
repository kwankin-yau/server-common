package com.lucendar.common.serv.cleanup;

import com.lucendar.common.types.ValidateResultReceiver;
import org.springframework.scheduling.support.CronExpression;

import java.util.StringJoiner;

public class QuotaCleanupConfig implements CleanupConfig, Cloneable {

    private boolean enabled;
    private String cron;
    private long quotaM;
    private long deleteM;
    private Long avgItemSize;

    public QuotaCleanupConfig() {
    }

    public QuotaCleanupConfig(boolean enabled, String cron, long quotaInM, long deleteInM, Long avgItemSize) {
        this.enabled = enabled;
        this.cron = cron;
        this.quotaM = quotaInM;
        this.deleteM = deleteInM;
        this.avgItemSize = avgItemSize;
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

    public long getQuotaM() {
        return quotaM;
    }

    public void setQuotaM(long quotaM) {
        this.quotaM = quotaM;
    }

    public long quota() {
        return quotaM * 1024 * 1024;
    }

    public long getDeleteM() {
        return deleteM;
    }

    public void setDeleteM(long deleteM) {
        this.deleteM = deleteM;
    }

    public long deleteBatchSize() {
        return deleteM * 1024 * 1024;
    }

    /**
     * Get average item size in bytes.
     *
     * @return average item size in bytes. null if not specified.
     */
    public Long getAvgItemSize() {
        return avgItemSize;
    }

    public void setAvgItemSize(Long avgItemSize) {
        this.avgItemSize = avgItemSize;
    }

    public void validate(ValidateResultReceiver receiver) {
        if (enabled) {
            if (cron == null || cron.isEmpty() || !CronExpression.isValidExpression(cron))
                receiver.invalidField("cron");

            if (quotaM <= 0)
                receiver.invalidField("quotaM");

            if (deleteM <= 0)
                receiver.invalidField("deleteM");

            if (avgItemSize < 0)
                receiver.invalidField("avgItemSize");
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", QuotaCleanupConfig.class.getSimpleName() + "[", "]")
                .add("enabled=" + enabled)
                .add("cron='" + cron + "'")
                .add("quotaM=" + quotaM)
                .add("deleteM=" + deleteM)
                .add("avgItemSize=" + avgItemSize)
                .toString();
    }

    @Override
    public QuotaCleanupConfig clone() {
        try {
            return (QuotaCleanupConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
