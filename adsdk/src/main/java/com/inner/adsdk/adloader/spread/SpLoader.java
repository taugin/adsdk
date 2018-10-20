package com.inner.adsdk.adloader.spread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.appub.ads.a.FSA;
import com.inner.adsdk.adloader.base.AbstractSdkLoader;
import com.inner.adsdk.config.SpConfig;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.manager.DataManager;
import com.inner.adsdk.stat.StatImpl;

import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018-10-19.
 */

public class SpLoader extends AbstractSdkLoader {

    private List<SpConfig> mSpreads;

    @Override
    public boolean isModuleLoaded() {
        return true;
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_SPREAD;
    }

    @Override
    public void loadInterstitial() {
        mSpreads = DataManager.get(mContext).getRemoteSpread();
        if (mSpreads != null && !mSpreads.isEmpty()) {
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onInterstitialLoaded();
            }
            StatImpl.get().reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        } else {
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
            }
        }
    }

    @Override
    public boolean isInterstitialLoaded() {
        return mSpreads != null && !mSpreads.isEmpty();
    }

    @Override
    public boolean showInterstitial() {
        if (mSpreads != null && !mSpreads.isEmpty()) {
            show();
            mSpreads = null;
            return true;
        }
        return false;
    }

    private void show() {
        try {
            int size = mSpreads.size();
            if (size > 0) {
                SpConfig spConfig = mSpreads.get(new Random(System.currentTimeMillis()).nextInt(size));
                Intent intent = new Intent(mContext, FSA.class);
                intent.putExtra(Intent.EXTRA_STREAM, spConfig);
                intent.putExtra(Intent.EXTRA_TITLE, getAdPlaceName());
                intent.putExtra(Intent.EXTRA_TEXT, getSdkName());
                intent.putExtra(Intent.EXTRA_TEMPLATE, getAdType());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                StatImpl.get().reportAdCallShow(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
                registerDismiss();
            }
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private void registerDismiss() {
        unregisterDismiss();
        try {
            mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(mContext.getPackageName() + ".action.SPDISMISS"));
        } catch(Exception e) {
        }
    }

    private void unregisterDismiss() {
        try {
            mContext.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterDismiss();
            if (getAdListener() != null) {
                getAdListener().onAdDismiss();
            }
        }
    };
}
