package com.rabbit.adsdk.adloader.mintegral;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.mbridge.msdk.MBridgeConstans;
import com.mbridge.msdk.MBridgeSDK;
import com.mbridge.msdk.out.AutoPlayMode;
import com.mbridge.msdk.out.Campaign;
import com.mbridge.msdk.out.Frame;
import com.mbridge.msdk.out.MBMultiStateEnum;
import com.mbridge.msdk.out.MBNativeAdvancedHandler;
import com.mbridge.msdk.out.MBNativeHandler;
import com.mbridge.msdk.out.MBridgeIds;
import com.mbridge.msdk.out.MBridgeSDKFactory;
import com.mbridge.msdk.out.NativeAdvancedAdListener;
import com.mbridge.msdk.out.NativeListener;
import com.mbridge.msdk.out.SDKInitStatusListener;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;
import com.rabbit.adsdk.utils.Utils;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/6/28.
 */

public class MintegralLoader extends AbstractSdkLoader {

    private static SDKInitializeState sSdkInitializeState = SDKInitializeState.SDK_STATE_UN_INITIALIZE;
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    private CountDownTimer mStateChecker;
    private MBridgeSDK mintegralSdk;
    private Campaign mCampaign;
    private MBNativeHandler mMBNativeHandler;
    private MBNativeAdvancedHandler mMBNativeAdvancedHandler;
    private ViewGroup mAdViewGroup;

    private MintegralBindView mintegralBindNativeView = new MintegralBindView();

    protected BaseBindNativeView getBaseBindNativeView() {
        return mintegralBindNativeView;
    }


    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_MINTEGRAL;
    }

    private void checkSdkInitializeState(final SDKInitializeListener sdkInitializeListener) {
        if (mStateChecker == null) {
            Log.iv(Log.TAG, getSdkName() + " sdk init start checking");
            mStateChecker = new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.iv(Log.TAG, getSdkName() + " sdk init state check");
                    if (sSdkInitializeState == SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS) {
                        if (mStateChecker != null) {
                            mStateChecker.cancel();
                            mStateChecker = null;
                        }
                        if (sdkInitializeListener != null) {
                            sdkInitializeListener.onInitializeSuccess(null, null);
                        }
                    }
                }

                @Override
                public void onFinish() {
                    Log.iv(Log.TAG, getSdkName() + " sdk init timeout");
                    mStateChecker = null;
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeFailure("timeout");
                    }
                }
            };
            mStateChecker.start();
        } else {
            Log.iv(Log.TAG, getSdkName() + " sdk initializing");
            if (sdkInitializeListener != null) {
                sdkInitializeListener.onInitializeFailure("initializing");
            }
        }
    }

    private void configSdkInit(final SDKInitializeListener sdkInitializeListener) {
        if (sSdkInitializeState == SDKInitializeState.SDK_STATE_INITIALIZING) {
            checkSdkInitializeState(sdkInitializeListener);
        } else {
            if (sSdkInitializeState == SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS) {
                if (sdkInitializeListener != null) {
                    sdkInitializeListener.onInitializeSuccess(null, null);
                }
                return;
            }
            sSdkInitializeState = SDKInitializeState.SDK_STATE_INITIALIZING;
            if (sHandler != null) {
                sHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sSdkInitializeState = SDKInitializeState.SDK_STATE_UN_INITIALIZE;
                        if (sdkInitializeListener != null) {
                            sdkInitializeListener.onInitializeFailure("timeout");
                        }
                    }
                }, 10000);
            }
            initMintegral(sdkInitializeListener);
        }
    }

    private void ensureSdk() {
        if (mintegralSdk == null) {
            mintegralSdk = MBridgeSDKFactory.getMBridgeSDK();
        }
    }

    private void initMintegral(final SDKInitializeListener sdkInitializeListener) {
        String appKey = null;
        String appId = null;
        PidConfig pidConfig = getPidConfig();
        if (pidConfig != null) {
            Map<String, String> pidExtra = pidConfig.getExtra();
            if (pidExtra != null) {
                appKey = pidExtra.get(Constant.APP_KEY);
                appId = pidExtra.get(Constant.APP_ID);
            }
        }
        Log.iv(Log.TAG, getSdkName() + " app id : " + appId + " , app key : " + appKey);
        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appKey)) {
            sSdkInitializeState = SDKInitializeState.SDK_STATE_INITIALIZE_FAILURE;
            if (sdkInitializeListener != null) {
                sdkInitializeListener.onInitializeFailure("app_id or app_key is null");
            }
            return;
        }
        ensureSdk();
        Map<String, String> configs = mintegralSdk.getMBConfigurationMap(appId, appKey);
        mintegralSdk.init(configs, mContext, new SDKInitStatusListener() {
            @Override
            public void onInitSuccess() {
                Log.iv(Log.TAG, getSdkName() + " sdk init successfully");
                if (sHandler != null) {
                    sHandler.removeCallbacksAndMessages(null);
                }
                sSdkInitializeState = SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS;
                if (sdkInitializeListener != null) {
                    sdkInitializeListener.onInitializeSuccess(null, null);
                }
            }

            @Override
            public void onInitFail(String s) {
                Log.iv(Log.TAG, getSdkName() + " sdk init failure : " + s);
                sSdkInitializeState = SDKInitializeState.SDK_STATE_INITIALIZE_FAILURE;
                if (sdkInitializeListener != null) {
                    sdkInitializeListener.onInitializeFailure("failure");
                }
            }
        });
    }

    @Override
    public void loadNative(Params params) {
        configSdkInit(new SDKInitializeListener() {
            @Override
            public void onInitializeSuccess(String appId, String appSecret) {
                loadNativeInternal(params);
            }

            @Override
            public void onInitializeFailure(String error) {
                Log.iv(Log.TAG, formatLog("init error : " + error));
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE);
            }
        });
    }

    @Override
    protected boolean checkPidConfig() {
        boolean superCheck = super.checkPidConfig();
        boolean pidExtraCheck = false;
        PidConfig pidConfig = getPidConfig();
        if (pidConfig != null) {
            Map<String, String> pidExtra = pidConfig.getExtra();
            if (pidExtra != null) {
                String unitId = pidExtra.get(Constant.UNIT_ID);
                String placementId = pidExtra.get(Constant.PLACEMENT_ID);
                if (!TextUtils.isEmpty(unitId) && !TextUtils.isEmpty(placementId)) {
                    pidExtraCheck = true;
                } else {
                    pidExtraCheck = false;
                    Log.iv(Log.TAG, formatLog("unit_id or placement_id is null"));
                }
            }
        }
        return superCheck && pidExtraCheck;
    }

    private void loadNativeInternal(Params params) {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG);
            return;
        }
        if (isNativeLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING);
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        PidConfig pidConfig = getPidConfig();
        String placementId = null;
        String unitId = null;
        if (pidConfig != null) {
            Map<String, String> pidExtra = pidConfig.getExtra();
            if (pidExtra != null) {
                unitId = pidExtra.get("unit_id");
                placementId = pidExtra.get("placement_id");
            }
        }
        if (TextUtils.isEmpty(placementId) || TextUtils.isEmpty(unitId)) {
            Log.iv(Log.TAG, formatLog("unit_id or placement_id is null"));
            return;
        }
        setLoading(true, STATE_REQUEST);
        if (isTemplateRendering()) {
            loadNativeTemplate(placementId, unitId);
        } else {
            loadNativeCustom(placementId, unitId);
        }
    }

    private void loadNativeCustom(String placementId, String unitId) {
        Map<String, Object> properties = MBNativeHandler.getNativeProperties(placementId, unitId);
        properties.put("ad_num", 1);
        properties.put("native_video_width", 720);
        properties.put("native_video_height", 480);
        properties.put("videoSupport", true);
        mMBNativeHandler = new MBNativeHandler(properties, this.mContext);
        mMBNativeHandler.setAdListener(new NativeListener.NativeAdListener() {
            @Override
            public void onAdLoaded(List<Campaign> campaigns, int i) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                reportAdLoaded();
                setLoading(false, STATE_SUCCESS);
                if (campaigns != null && campaigns.size() != 0) {
                    mCampaign = (Campaign) campaigns.get(0);
                    putCachedAdTime(mCampaign);
                    notifySdkLoaderLoaded(false);
                } else {
                    notifyAdLoadFailed(Constant.AD_ERROR_NOFILL);
                }
            }

            @Override
            public void onAdLoadError(String s) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + s, true));
                reportAdError(s);
                setLoading(false, STATE_FAILURE);
                notifyAdLoadFailed(Constant.AD_ERROR_NOFILL);
            }

            @Override
            public void onAdClick(Campaign campaign) {
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdFramesLoaded(List<Frame> list) {
            }

            @Override
            public void onLoggingImpression(int i) {
                Log.iv(Log.TAG, formatLog("ad impression"));
                reportAdImp(null);
                notifyAdImp(null);
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mMBNativeHandler.load();
    }

    private int parseInt(String text, int defaultValue) {
        int value = defaultValue;
        if (!TextUtils.isEmpty(text)) {
            try {
                value = Integer.parseInt(text);
            } catch (Exception e) {
                value = defaultValue;
            }
        }
        return value;
    }

    private Pair<Integer, Integer> getNativeViewSize() {
        if (mPidConfig != null) {
            Map<String, String> extra = mPidConfig.getExtra();
            if (extra != null) {
                String widthText = extra.get(Constant.WIDTH);
                String heightText = extra.get(Constant.HEIGHT);
                int width = parseInt(widthText, 0);
                int height = parseInt(heightText, 0);
                if (width > 0 && height > 0) {
                    return new Pair<>(width, height);
                }
            }
        }
        int screenWidth = Utils.getScreenWidth(mContext);
        int height = (int) ((float)screenWidth * 250 / 320);
        return new Pair<>(screenWidth, height);
    }

    private void loadNativeTemplate(String placementId, String unitId) {
        mMBNativeAdvancedHandler = new MBNativeAdvancedHandler(getActivity(), placementId, unitId);
        Pair<Integer, Integer> pair = getNativeViewSize();
        int width = 320;
        int height = 250;
        if (pair != null) {
            width = pair.first;
            height = pair.second;
        }
        mMBNativeAdvancedHandler.setNativeViewSize(width, height);
        mMBNativeAdvancedHandler.setPlayMuteState(MBridgeConstans.REWARD_VIDEO_PLAY_MUTE);
        mMBNativeAdvancedHandler.setCloseButtonState(MBMultiStateEnum.positive);
        mMBNativeAdvancedHandler.autoLoopPlay(AutoPlayMode.PLAY_WHEN_USER_CLICK);
        mMBNativeAdvancedHandler.setAdListener(new NativeAdvancedAdListener() {
            @Override
            public void onLoadFailed(MBridgeIds mBridgeIds, String s) {
                Log.iv(Log.TAG, formatLog("ad load failed : " + s, true));
                reportAdError(s);
                setLoading(false, STATE_FAILURE);
                notifyAdLoadFailed(Constant.AD_ERROR_NOFILL);
            }

            @Override
            public void onLoadSuccessed(MBridgeIds mBridgeIds) {
                String requestId = mMBNativeAdvancedHandler.getRequestId();
                Log.iv(Log.TAG, formatLog("ad load success req id : " + requestId));
                reportAdLoaded();
                setLoading(false, STATE_SUCCESS);
                mAdViewGroup = mMBNativeAdvancedHandler.getAdViewGroup();
                putCachedAdTime(mAdViewGroup);
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onLogImpression(MBridgeIds mBridgeIds) {
                Log.iv(Log.TAG, formatLog("ad impression"));
                reportAdImp("template");
                notifyAdImp("template");
            }

            @Override
            public void onClick(MBridgeIds mBridgeIds) {
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onLeaveApp(MBridgeIds mBridgeIds) {
            }

            @Override
            public void showFullScreen(MBridgeIds mBridgeIds) {
            }

            @Override
            public void closeFullScreen(MBridgeIds mBridgeIds) {
            }

            @Override
            public void onClose(MBridgeIds mBridgeIds) {
                Log.iv(Log.TAG, formatLog("ad close"));
                reportAdClose();
                notifyAdDismiss();
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mMBNativeAdvancedHandler.load();
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = false;
        if (isTemplateRendering()) {
            loaded = isTemplateNativeLoaded();
        } else {
            loaded = isCustomNativeLoaded();
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    private boolean isCustomNativeLoaded() {
        return mCampaign != null && !isCachedAdExpired(mCampaign);
    }

    private boolean isTemplateNativeLoaded() {
        return mAdViewGroup != null  && !isCachedAdExpired(mAdViewGroup);
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (isTemplateRendering()) {
            showTemplateNative(viewGroup);
        } else {
            showCustomNative(viewGroup, params);
        }
    }

    private void showCustomNative(ViewGroup viewGroup, Params params) {
        if (mCampaign != null) {
            clearCachedAdTime(mCampaign);
            mintegralBindNativeView.bindMintegralNative(params, mContext, viewGroup, mCampaign, mPidConfig, mMBNativeHandler);
            mCampaign = null;
            reportAdShow();
            notifyAdShow();
        }
    }

    private void showTemplateNative(ViewGroup viewGroup) {
        if (mAdViewGroup != null) {
            clearCachedAdTime(mAdViewGroup);
            viewGroup.removeAllViews();
            ViewParent viewParent = mAdViewGroup.getParent();
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(mAdViewGroup);
            }
            viewGroup.addView(mAdViewGroup);
            if (viewGroup.getVisibility() != View.VISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
            }
            mAdViewGroup = null;
            reportAdShow();
            notifyAdShow();
        }
    }
}
