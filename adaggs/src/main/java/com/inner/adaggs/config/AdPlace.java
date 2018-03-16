package com.inner.adaggs.config;

import android.text.TextUtils;

import com.inner.adaggs.constant.Constant;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdPlace {

    private String name;

    private String mode; // seq : 顺序请求, con : 并发请求, ran : 随机请求

    private List<PidConfig> pidsList;

    private int maxCount;

    private int percent;

    private String ptype; // inner : 应用内(默认), outer : 应用外, film : 贴片

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<PidConfig> getPidsList() {
        return pidsList;
    }

    public void setPidsList(List<PidConfig> pidsList) {
        this.pidsList = pidsList;
        setPidNames();
    }

    private void setPidNames() {
        if (pidsList != null && !pidsList.isEmpty()) {
            for (PidConfig config : pidsList) {
                if (config != null) {
                    config.setName(name);
                }
            }
        }
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public boolean isSequence() {
        return TextUtils.equals(Constant.MODE_SEQ, getMode());
    }

    public boolean isConcurrent() {
        return TextUtils.equals(Constant.MODE_CON, getMode());
    }

    public boolean isRandom() {
        return TextUtils.equals(Constant.MODE_RAN, getMode());
    }

    public String getPtype() {
        return ptype;
    }

    public void setPtype(String ptype) {
        this.ptype = ptype;
    }

    @Override
    public String toString() {
        return "AdPlace{" +
                "name='" + name + '\'' +
                ", mode='" + mode + '\'' +
                ", pidsList=" + pidsList +
                ", maxCount=" + maxCount +
                ", percent=" + percent +
                '}';
    }
}
