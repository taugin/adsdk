package com.inner.adsdk.config;

/**
 * Created by Administrator on 2018/5/5.
 */

public class AdSwitch {
    // 阻塞loading
    private boolean blockLoading;
    // 上报错误日志
    private boolean reportError;
    // 上报加载时间
    private boolean reportTime;
    // 上报友盟
    private boolean reportUmeng;
    // 上报Firebase
    private boolean reportFirebase;
    // 上报Appsflyer
    private boolean reportAppsflyer;
    // 上报Facebook
    private boolean reportFacebook;
    // gt 与 tt 互斥
    private boolean gtAtExclusive;

    private boolean forbidFromInsights;

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

    public boolean isGtAtExclusive() {
        return gtAtExclusive;
    }

    public void setGtAtExclusive(boolean gtAtExclusive) {
        this.gtAtExclusive = gtAtExclusive;
    }

    public boolean isForbidFromInsights() {
        return forbidFromInsights;
    }

    public void setForbidFromInsights(boolean forbidFromInsights) {
        this.forbidFromInsights = forbidFromInsights;
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
