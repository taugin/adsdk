package com.bacad.ioc.gsb.base;

/**
 * Created by Administrator on 2018/5/5.
 */

public class ScFl {
    public static final String SCFL_NAME = "scene_flag";

    // 上报友盟
    private boolean reportUmeng = true;
    // 上报Firebase
    private boolean reportFirebase = true;
    // 上报Facebook
    private boolean reportFacebook = true;

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
                ", ru=" + reportUmeng +
                ", rf=" + reportFirebase +
                ", rfb=" + reportFacebook +
                '}';
    }
}
