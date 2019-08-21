package com.hauyu.adsdk.data.config;

/**
 * Created by Administrator on 2018/5/5.
 */

public class AdSwitch {
    // 上报错误日志
    private boolean reportError;
    // 上报加载时间
    private boolean reportTime;
    // 上报友盟
    private boolean reportUmeng;
    // 上报Firebase
    private boolean reportFirebase;
    // 上报Facebook
    private boolean reportFacebook;

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

    public boolean isReportFacebook() {
        return reportFacebook;
    }

    public void setReportFacebook(boolean reportFacebook) {
        this.reportFacebook = reportFacebook;
    }

    @Override
    public String toString() {
        return "ads{" +
                ", re=" + reportError +
                ", rt=" + reportTime +
                ", ru=" + reportUmeng +
                ", rf=" + reportFirebase +
                ", rfb=" + reportFacebook +
                '}';
    }
}
