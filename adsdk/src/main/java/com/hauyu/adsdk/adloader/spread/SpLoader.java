package com.hauyu.adsdk.adloader.spread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.appub.ads.a.FSA;
import com.hauyu.adsdk.adloader.base.AbstractSdkLoader;
import com.hauyu.adsdk.config.SpConfig;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.manager.DataManager;
import com.hauyu.adsdk.stat.StatImpl;
import com.hauyu.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018-10-19.
 */

public class SpLoader extends AbstractSdkLoader {

    private List<SpConfig> mSpreads;

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_SPREAD;
    }

    @Override
    public void loadInterstitial() {
        mSpreads = checkSpConfig(DataManager.get(mContext).getRemoteSpread());
        if (mSpreads != null && !mSpreads.isEmpty()) {
            if (getAdListener() != null) {
                setLoadedFlag();
                getAdListener().onInterstitialLoaded(this);
            }
            StatImpl.get().reportAdLoaded(mContext, getAdPlaceName(), getSdkName(), getAdType(), null);
        } else {
            if (getAdListener() != null) {
                getAdListener().onAdFailed(Constant.AD_ERROR_LOAD);
            }
        }
    }

    /**
     * 检出有效的配置
     * @param spList
     * @return
     */
    private List<SpConfig> checkSpConfig(List<SpConfig> spList) {
        if (spList == null || spList.isEmpty()) {
            return null;
        }
        mSpreads = new ArrayList<SpConfig>();
        for (SpConfig config : spList) {
            // 参数有效，并且未安装
            if (checkArgs(config) && !Utils.isInstalled(mContext, config.getPkgname()) && !config.isDisable()) {
                mSpreads.add(config);
            }
        }
        return mSpreads;
    }

    /**
     * 检查参数合法性
     * @param spConfig
     * @return
     */
    private boolean checkArgs(SpConfig spConfig) {
        if (spConfig == null) {
            return false;
        }
        if (TextUtils.isEmpty(spConfig.getType()) || TextUtils.equals(SpConfig.TYPE_APP, spConfig.getType())) {
            if (TextUtils.isEmpty(spConfig.getBanner())
                    || TextUtils.isEmpty(spConfig.getIcon())
                    || TextUtils.isEmpty(spConfig.getTitle())
                    || TextUtils.isEmpty(spConfig.getPkgname())
                    || TextUtils.isEmpty(spConfig.getDetail())
                    || TextUtils.isEmpty(spConfig.getCta())) {
                return false;
            }
        } else if (TextUtils.equals(SpConfig.TYPE_URL, spConfig.getType())) {
            if (TextUtils.isEmpty(spConfig.getLinkUrl())) {
                return false;
            }
        } else if (TextUtils.equals(SpConfig.TYPE_HTML, spConfig.getType())) {
            if (TextUtils.isEmpty(spConfig.getHtml())) {
                return false;
            }
        } else {
            if (TextUtils.isEmpty(spConfig.getBanner())
                    || TextUtils.isEmpty(spConfig.getIcon())
                    || TextUtils.isEmpty(spConfig.getTitle())
                    || TextUtils.isEmpty(spConfig.getPkgname())
                    || TextUtils.isEmpty(spConfig.getDetail())
                    || TextUtils.isEmpty(spConfig.getCta())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isInterstitialLoaded() {
        return mSpreads != null && !mSpreads.isEmpty();
    }

    @Override
    public boolean showInterstitial() {
        if (mSpreads != null && !mSpreads.isEmpty()) {
            show();
            mSpreads.clear();
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
                Intent intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action.AFPICKER");
                if (intent == null) {
                    intent = new Intent(mContext, FSA.class);
                }
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
