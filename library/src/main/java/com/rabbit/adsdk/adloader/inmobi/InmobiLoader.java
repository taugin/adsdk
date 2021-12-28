package com.rabbit.adsdk.adloader.inmobi;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.inmobi.ads.listeners.NativeAdEventListener;
import com.inmobi.sdk.InMobiSdk;
import com.inmobi.sdk.SdkInitializationListener;
import com.rabbit.adsdk.adloader.base.AbstractSdkLoader;
import com.rabbit.adsdk.adloader.base.BaseBindNativeView;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.core.framework.Params;
import com.rabbit.adsdk.data.config.PidConfig;
import com.rabbit.adsdk.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Administrator on 2018/6/28.
 */

public class InmobiLoader extends AbstractSdkLoader {

    private static SDKInitializeState sSdkInitializeState = SDKInitializeState.SDK_STATE_UN_INITIALIZE;
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    private CountDownTimer mStateChecker;
    private InMobiNative mInMobiNative;

    private InmobiBindView inmobiBindNativeView = new InmobiBindView();

    protected BaseBindNativeView getBaseBindNativeView() {
        return inmobiBindNativeView;
    }


    @Override
    public void init(Context context, PidConfig pidConfig) {
        super.init(context, pidConfig);
    }

    @Override
    public String getSdkName() {
        return Constant.AD_SDK_INMOBI;
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
            initInmobi(sdkInitializeListener);
        }
    }

    private void initInmobi(final SDKInitializeListener sdkInitializeListener) {
        String accountId = null;
        PidConfig pidConfig = getPidConfig();
        if (pidConfig != null) {
            Map<String, String> pidExtra = pidConfig.getExtra();
            if (pidExtra != null) {
                accountId = pidExtra.get(Constant.ACCOUNT_ID);
            }
        }
        Log.iv(Log.TAG, getSdkName() + " account_id : " + accountId);
        if (TextUtils.isEmpty(accountId)) {
            sSdkInitializeState = SDKInitializeState.SDK_STATE_INITIALIZE_FAILURE;
            if (sdkInitializeListener != null) {
                sdkInitializeListener.onInitializeFailure("account_id is null");
            }
            return;
        }

        JSONObject consentObject = new JSONObject();
        try {
            // Provide correct consent value to sdk which is obtained by User
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
            // Provide 0 if GDPR is not applicable and 1 if applicable
            consentObject.put("gdpr", "0");
            // Provide user consent in IAB format
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB, 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        InMobiSdk.init(mContext, accountId, consentObject, new SdkInitializationListener() {
            @Override
            public void onInitializationComplete(Error error) {
                Log.iv(Log.TAG, getSdkName() + " sdk init successfully");
                if (sHandler != null) {
                    sHandler.removeCallbacksAndMessages(null);
                }
                if (error != null) {
                    Log.iv(Log.TAG, getSdkName() + " sdk init failure : " + error);
                    sSdkInitializeState = SDKInitializeState.SDK_STATE_INITIALIZE_FAILURE;
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeFailure("failure");
                    }
                } else {
                    sSdkInitializeState = SDKInitializeState.SDK_STATE_INITIALIZE_SUCCESS;
                    if (sdkInitializeListener != null) {
                        sdkInitializeListener.onInitializeSuccess(null, null);
                    }
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
                notifyAdLoadFailed(Constant.AD_ERROR_INITIALIZE, "init error : " + error);
            }
        });
    }

    private void loadNativeInternal(Params params) {
        if (!checkPidConfig()) {
            Log.iv(Log.TAG, formatLog("config error"));
            notifyAdLoadFailed(Constant.AD_ERROR_CONFIG, "config error");
            return;
        }
        if (isNativeLoaded()) {
            Log.iv(Log.TAG, formatLog("already loaded"));
            notifySdkLoaderLoaded(true);
            return;
        }
        if (isLoading()) {
            Log.iv(Log.TAG, formatLog("already loading"));
            notifyAdLoadFailed(Constant.AD_ERROR_LOADING, "already loading");
            return;
        }

        // 检测通用配置是否符合
        if (!checkCommonConfig()) {
            return;
        }

        long placementId = 0;
        try {
            placementId = Long.parseLong(getPid());
        } catch (Exception e) {
        }
        setLoading(true, STATE_REQUEST);
        mInMobiNative = new InMobiNative(mContext, placementId, new NativeAdEventListener() {

            @Override
            public void onAdLoadSucceeded(InMobiNative inMobiNative, AdMetaInfo adMetaInfo) {
                Log.iv(Log.TAG, formatLog("ad load success"));
                reportAdLoaded();
                setLoading(false, STATE_SUCCESS);
                putCachedAdTime(inMobiNative);
                notifySdkLoaderLoaded(false);
            }

            @Override
            public void onAdImpressed(InMobiNative inMobiNative) {
                Log.iv(Log.TAG, formatLog("ad impression"));
                reportAdImp(null);
                notifyAdImp(null);
            }

            @Override
            public void onAdClicked(InMobiNative inMobiNative) {
                Log.iv(Log.TAG, formatLog("ad click"));
                reportAdClick();
                notifyAdClick();
            }

            @Override
            public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
                if (inMobiAdRequestStatus != null) {
                    Log.iv(Log.TAG, formatLog("ad load failed : " + inMobiAdRequestStatus.getMessage(), true));
                    reportAdError(inMobiAdRequestStatus.getMessage());
                }
                setLoading(false, STATE_FAILURE);
                notifyAdLoadFailed(toSdkError(inMobiAdRequestStatus), toErrorMessage(inMobiAdRequestStatus));
            }

            @Override
            public void onAdFullScreenDismissed(InMobiNative inMobiNative) {
                Log.iv(Log.TAG, formatLog("ad dismiss"));
                reportAdClose();
                notifyAdDismiss();
            }
        });
        printInterfaceLog(ACTION_LOAD);
        reportAdRequest();
        notifyAdRequest();
        mInMobiNative.load();
    }

    @Override
    public boolean isNativeLoaded() {
        boolean loaded = false;
        if (mInMobiNative != null) {
            loaded = mInMobiNative.isReady() && !isCachedAdExpired(mInMobiNative);
        }
        if (loaded) {
            Log.iv(Log.TAG, formatLog("ad loaded : " + loaded));
        }
        return loaded;
    }

    @Override
    public void showNative(ViewGroup viewGroup, Params params) {
        printInterfaceLog(ACTION_SHOW);
        if (mInMobiNative != null) {
            clearCachedAdTime(mInMobiNative);
            inmobiBindNativeView.bindInmobiNative(params, mContext, viewGroup, mInMobiNative, mPidConfig, null);
            mInMobiNative = null;
            reportAdShow();
            notifyAdShow();
        } else {
            Log.e(Log.TAG, formatShowErrorLog("InMobiNative is null"));
            notifyAdShowFailed(Constant.AD_ERROR_SHOW, "show " + getSdkName() + " " + getAdType() + " error : InMobiNative is null");
        }
    }

    protected int toSdkError(InMobiAdRequestStatus status) {
        InMobiAdRequestStatus.StatusCode code = InMobiAdRequestStatus.StatusCode.NO_FILL;
        if (status != null) {
            code = status.getStatusCode();
            if (code == InMobiAdRequestStatus.StatusCode.INTERNAL_ERROR) {
                return Constant.AD_ERROR_INTERNAL;
            }
            if (code == InMobiAdRequestStatus.StatusCode.INVALID_RESPONSE_IN_LOAD) {
                return Constant.AD_ERROR_INVALID_REQUEST;
            }
            if (code == InMobiAdRequestStatus.StatusCode.NETWORK_UNREACHABLE) {
                return Constant.AD_ERROR_NETWORK;
            }
            if (code == InMobiAdRequestStatus.StatusCode.NO_FILL) {
                return Constant.AD_ERROR_NOFILL;
            }
            if (code == InMobiAdRequestStatus.StatusCode.REQUEST_TIMED_OUT) {
                return Constant.AD_ERROR_TIMEOUT;
            }
            if (code == InMobiAdRequestStatus.StatusCode.SERVER_ERROR) {
                return Constant.AD_ERROR_SERVER;
            }
        }
        return Constant.AD_ERROR_UNKNOWN;
    }

    private String toErrorMessage(InMobiAdRequestStatus status) {
        if (status != null) {
            return "[" + status.getStatusCode() + "] " + status.getMessage();
        }
        return null;
    }
}
