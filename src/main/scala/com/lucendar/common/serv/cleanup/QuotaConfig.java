package com.lucendar.common.serv.cleanup;

public class QuotaConfig {
    private boolean enabled;
    private long quotaInM;
    private long deleteInM;
    private Long avgItemSize;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        return "QuotaConfig{" +
                "enabled=" + enabled +
                ", quotaInM=" + quotaInM +
                ", deleteInM=" + deleteInM +
                '}';
    }
}
