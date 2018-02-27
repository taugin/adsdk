package com.inner.adaggs.config;

import android.text.TextUtils;

import com.inner.adaggs.constant.Constant;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdPlace {

    private String name;

    private String mode;

    private List<PidConfig> pidsList;

    private int maxCount;

    private int percent;

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
        return TextUtils.equals(Constant.MODE_SEQ, mode);
    }

    public boolean isConcurrent() {
        return TextUtils.equals(Constant.MODE_CON, mode);
    }

    public boolean isRandom() {
        return TextUtils.equals(Constant.MODE_RAN, mode);
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
