package com.inner.adsdk.config;

/**
 * Created by Administrator on 2018/5/5.
 */

public class AdSwitch {
    private boolean blockLoading;
    private boolean reportError;
    private boolean reportTime;
    private boolean reportUmeng;
    private boolean reportFirebase;
    private boolean reportAppsflyer;
    private boolean reportFacebook;

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

    public boolean isReportTime() {
        return reportTime;
    }

    public void setReportTime(boolean reportTime) {
        this.reportTime = reportTime;
    }

    public boolean isReportUmeng() {
        return reportUmeng;
    }

    public void setReportUmeng(boolean reportUmeng) {
        this.reportUmeng = reportUmeng;
    }

    public boolean isReportFirebase() {
        return reportFirebase;
    }

    public void setReportFirebase(boolean reportFirebase) {
        this.reportFirebase = reportFirebase;
    }

    public boolean isReportAppsflyer() {
        return reportAppsflyer;
    }

    public void setReportAppsflyer(boolean reportAppsflyer) {
        this.reportAppsflyer = reportAppsflyer;
    }

    public boolean isReportFacebook() {
        return reportFacebook;
    }

    public void setReportFacebook(boolean reportFacebook) {
        this.reportFacebook = reportFacebook;
    }

    @Override
    public String toString() {
        return "ads{" +
                "bl=" + blockLoading +
                ", re=" + reportError +
                ", rt=" + reportTime +
                ", ru=" + reportUmeng +
                ", rf=" + reportFirebase +
                ", ra=" + reportAppsflyer +
                ", rfb=" + reportFacebook +
                '}';
    }
}
