package com.inner.adsdk.config;

import android.text.TextUtils;

import com.inner.adsdk.constant.Constant;

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

    private boolean autoSwitch;

    private String uniqueValue;

    private boolean loadOnlyOnce = true;

    private int ecpmSort;

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
        setAdPlaceNames();
    }

    private void setAdPlaceNames() {
        if (pidsList != null && !pidsList.isEmpty()) {
            for (PidConfig config : pidsList) {
                if (config != null) {
                    config.setAdPlaceName(name);
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

    public boolean isAutoSwitch() {
        return autoSwitch;
    }

    public void setAutoSwitch(boolean autoSwitch) {
        this.autoSwitch = autoSwitch;
    }

    public String getUniqueValue() {
        return uniqueValue;
    }

    public void setUniqueValue(String uniqueValue) {
        this.uniqueValue = uniqueValue;
    }

    public boolean isLoadOnlyOnce() {
        return loadOnlyOnce;
    }

    public void setLoadOnlyOnce(boolean loadOnlyOnce) {
        this.loadOnlyOnce = loadOnlyOnce;
    }

    public int getEcpmSort() {
        return ecpmSort;
    }

    public void setEcpmSort(int ecpmSort) {
        this.ecpmSort = ecpmSort;
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

    @Override
    public String toString() {
        return "AdPlace{" +
                "name='" + name + '\'' +
                ", mode='" + mode + '\'' +
                ", pidsList=" + pidsList +
                ", maxCount=" + maxCount +
                ", percent=" + percent +
                ", autoSwitch=" + autoSwitch +
                ", uniqueValue='" + uniqueValue + '\'' +
                ", loadOnlyOnce=" + loadOnlyOnce +
                '}';
    }
}
