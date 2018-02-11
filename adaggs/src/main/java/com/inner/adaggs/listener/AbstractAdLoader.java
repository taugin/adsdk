package com.inner.adaggs.listener;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.inner.adaggs.config.PidConfig;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.stat.IStat;

/**
 * Created by Administrator on 2018/2/9.
 */

public class AbstractAdLoader implements IAdLoader {

    protected PidConfig mPidConfig;
    protected Context mContext;
    protected IStat mStat;

    protected OnAdListener mOnAdListener;
    protected OnInterstitialListener mOnInterstitialListener;

    @Override
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public String getSdkName() {
        return null;
    }

    @Override
    public void setPidConfig(PidConfig config) {
        mPidConfig = config;
    }

    @Override
    public PidConfig getPidConfig() {
        return mPidConfig;
    }

    @Override
    public void setOnInterstitialListener(OnInterstitialListener l) {
        mOnInterstitialListener = l;
    }

    @Override
    public void loadInterstitial() {
    }

    @Override
    public boolean showInterstitial() {
        return false;
    }

    @Override
    public void loadNative(View rootView, int templateId) {
    }

    @Override
    public void showNative(ViewGroup viewGroup) {

    }

    @Override
    public void setOnAdListener(OnAdListener l) {
        mOnAdListener = l;
    }

    @Override
    public void loadBanner(int adSize) {
    }

    @Override
    public void showBanner(ViewGroup viewGroup) {
    }

    @Override
    public View getAdView() {
        return null;
    }

    @Override
    public boolean isInterstitialLoaded() {
        return false;
    }

    @Override
    public boolean isBannerLoaded() {
        return false;
    }

    @Override
    public boolean isNativeLoaded() {
        return false;
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getPidName() {
        if (mPidConfig != null) {
            return mPidConfig.getName();
        }
        return null;
    }

    @Override
    public boolean isBannerType() {
        return false;
    }

    @Override
    public boolean isNativeType() {
        return false;
    }

    @Override
    public boolean isInterstitialType() {
        return false;
    }

    protected boolean checkPidConfig() {
        if (mPidConfig == null) {
            Log.e(Log.TAG, "pidconfig is null");
            return false;
        }
        if (!TextUtils.equals(mPidConfig.getSdk(), getSdkName())) {
            Log.e(Log.TAG, "sdk not equals");
            return false;
        }
        if (TextUtils.isEmpty(mPidConfig.getPid())) {
            Log.e(Log.TAG, "pid is empty");
            return false;
        }
        return true;
    }
}
