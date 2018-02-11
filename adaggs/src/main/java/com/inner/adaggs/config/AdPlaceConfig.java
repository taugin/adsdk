package com.inner.adaggs.config;

import android.text.TextUtils;

import com.inner.adaggs.constant.Constant;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AdPlaceConfig {

    private String name;

    private String mode;

    private List<PidConfig> pidsList;

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
        return "AdPlaceConfig{" +
                "name='" + name + '\'' +
                ", mode='" + mode + '\'' +
                ", pidsList=" + pidsList +
                '}';
    }
}
