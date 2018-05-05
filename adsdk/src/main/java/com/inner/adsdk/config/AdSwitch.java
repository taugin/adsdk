package com.inner.adsdk.config;

/**
 * Created by Administrator on 2018/5/5.
 */

public class AdSwitch {
    private boolean blockLoading;
    private boolean reportError;

    public boolean isBlockLoading() {
        return blockLoading;
    }

    public void setBlockLoading(boolean blockLoading) {
        this.blockLoading = blockLoading;
    }

    public boolean isReportError() {
        return reportError;
    }

    public void setReportError(boolean reportError) {
        this.reportError = reportError;
    }
}
