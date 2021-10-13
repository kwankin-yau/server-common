package com.lucendar.common.serv.cleanup;

public class QuotaCleanupConfig implements CleanupConfig {

    private boolean enabled;
    private String cron;
    private long quotaInM;
    private long deleteInM;
    private Long avgItemSize;

    public QuotaCleanupConfig() {
    }

    public QuotaCleanupConfig(boolean enabled, String cron, long quotaInM, long deleteInM, Long avgItemSize) {
        this.enabled = enabled;
        this.cron = cron;
        this.quotaInM = quotaInM;
        this.deleteInM = deleteInM;
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

    public long getQuotaInM() {
        return quotaInM;
    }

    public void setQuotaInM(long quotaInM) {
        this.quotaInM = quotaInM;
    }

    public long quota() {
        return quotaInM * 1024 * 1024;
    }

    public long getDeleteInM() {
        return deleteInM;
    }

    public void setDeleteInM(long deleteInM) {
        this.deleteInM = deleteInM;
    }

    public long deleteBatchSize() {
        return deleteInM * 1024 * 1024;
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

    @Override
    public String toString() {
        return "QuotaCleanupConfig{" +
                "enabled=" + enabled +
                ", cron='" + cron + '\'' +
                ", quotaInM=" + quotaInM +
                ", deleteInM=" + deleteInM +
                ", avgItemSize=" + avgItemSize +
                '}';
    }
}
