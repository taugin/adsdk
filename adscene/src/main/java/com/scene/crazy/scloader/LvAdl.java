package com.scene.crazy.scloader;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.scene.crazy.base.Bldr;
import com.scene.crazy.base.CSvr;
import com.scene.crazy.data.SceneData;
import com.scene.crazy.event.SceneEventImpl;
import com.scene.crazy.scpolicy.LvPcy;
import com.rabbit.adsdk.AdSdk;
import com.rabbit.adsdk.constant.Constant;
import com.rabbit.adsdk.listener.SimpleAdSdkListener;
import com.scene.crazy.log.Log;
import com.rabbit.adsdk.utils.Utils;

import java.util.Locale;

/**
 * Created by Administrator on 2019/8/18.
 */

public class LvAdl extends Bldr {

    private static final int MSG_SHOW_LOCKSCREEN = 0x365675;
    private static final int DELAY = 8000;
    private static LvAdl sLvAdl;

    private Context mContext;
    private AdSdk mAdSdk;
    private Handler mHandler = new Handler();

    private LvAdl(Context context) {
        super(LvPcy.get(context));
        mContext = context.getApplicationContext();
        CSvr.get(context).registerTriggerListener(this);
    }

    public static LvAdl get(Context context) {
        if (sLvAdl == null) {
            create(context);
        }
        return sLvAdl;
    }

    private static void create(Context context) {
        synchronized (LvAdl.class) {
            if (sLvAdl == null) {
                sLvAdl = new LvAdl(context);
            }
        }
    }

    public void init() {
        mAdSdk = AdSdk.get(mContext);
        if (mAdSdk == null) {
            return;
        }
        LvPcy.get(mContext).init();
        updateLtPolicy();
    }

    @Override
    protected Context getContext() {
        return mContext;
    }

    @Override
    public void onScreenOn(Context context) {
        if (!isKeyguardSecure()) {
            showLockScreen();
        }
    }

    @Override
    public void onUserPresent(Context context) {
        if (isKeyguardSecure()) {
            showLockScreen();
        }
    }

    private void showLockScreen() {
        /*
        if (mHandler != null) {
            if (!mHandler.hasMessages(MSG_SHOW_LOCKSCREEN)) {
                mHandler.sendEmptyMessageDelayed(MSG_SHOW_LOCKSCREEN, DELAY);
                showForScreenOn();
            }
        }
        */
        showForScreenOn();
    }

    private void showForScreenOn() {
        LvPcy lvPcy = LvPcy.get(mContext);
        if (lvPcy != null) {
            String ltType = lvPcy.getLtType();
            if (lvPcy.isFullScreen(ltType)) {
                fireFullScreen();
            } else if (lvPcy.isLockScreen(ltType)) {
                fireLockScreen();
            } else {
                fireLockScreen();
            }
        }
    }

    private void fireLockScreen() {
        updateLtPolicy();
        if (!LvPcy.get(mContext).isLtAllowed()) {
            return;
        }
        String placeName = getAdMainName();
        if (TextUtils.isEmpty(placeName)) {
            return;
        }
        String pType = LvPcy.get(mContext).getType();
        try {
            String action = null;
            if (!TextUtils.isEmpty(pType)) {
                String actType = pType.replace("t", "a");
                action = actType.toUpperCase(Locale.getDefault()) + "VIEW";
            }
            Log.iv(Log.TAG, "filter : " + action);
            Intent intent = null;
            if (!TextUtils.isEmpty(action)) {
                intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action." + action, false, false);
            }
            if (intent == null) {
                intent = new Intent();
                ComponentName cmp = new ComponentName(mContext, Utils.getActivityNameByAction(getContext(), getContext().getPackageName() + ".action.MATCH_DOING"));
                intent.setComponent(cmp);
            }
            intent.putExtra(Intent.EXTRA_TITLE, placeName);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            intent.putExtra(Intent.EXTRA_REPLACING, pType);
            userFullIntent(getContext(), intent);
            try {
                PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();
            } catch (Exception e) {
                try {
                    mContext.startActivity(intent);
                } catch (Exception e1) {
                }
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "error : " + e);
        }
    }

    private void fireFullScreen() {
        if (mAdSdk != null) {
            updateLtPolicy();
            if (!LvPcy.get(mContext).isLtAllowed()) {
                return;
            }
            String placeName = getAdMainName();
            if (TextUtils.isEmpty(placeName)) {
                Log.iv(Log.TAG, getType() + " not found place name");
                return;
            }
            if (LvPcy.get(mContext).isLoading()) {
                Log.iv(Log.TAG, getType() + " is loading");
                return;
            }
            Log.iv(Log.TAG, "");
            LvPcy.get(mContext).setLoading(true);
            SceneEventImpl.get().reportAdSceneRequest(mContext, LvPcy.get(mContext).getType(), placeName);
            mAdSdk.loadComplexAds(placeName, generateAdParams(), new SimpleAdSdkListener() {
                @Override
                public void onLoaded(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "loaded pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    SceneEventImpl.get().reportAdSceneLoaded(mContext, LvPcy.get(mContext).getType(), pidName);
                    LvPcy.get(mContext).setLoading(false);
                    if (LvPcy.get(mContext).isLtAllowed()) {
                        if (LvPcy.get(mContext).isShowBottom()
                                || Constant.TYPE_BANNER.equals(adType)
                                || Constant.TYPE_NATIVE.equals(adType)) {
                            show(pidName, source, adType, LvPcy.get(mContext).getType());
                        } else {
                            AdSdk.get(mContext).showComplexAds(pidName, null);
                        }
                        SceneEventImpl.get().reportAdSceneShow(mContext, LvPcy.get(mContext).getType(), pidName);
                    } else {
                        SceneEventImpl.get().reportAdSceneDisallow(mContext, LvPcy.get(mContext).getType(), pidName);
                    }
                }

                @Override
                public void onDismiss(String pidName, String source, String adType, boolean onDestroy) {
                    Log.iv(Log.TAG, "dismiss pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    LvPcy.get(mContext).reportImpression(false);
                    if (!TextUtils.equals(source, Constant.AD_SDK_SPREAD)
                            && LvPcy.get(mContext).isShowBottom()
                            && !Constant.TYPE_BANNER.equals(adType)
                            && !Constant.TYPE_NATIVE.equals(adType)) {
                        hide();
                    }
                }

                @Override
                public void onImp(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "show pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    LvPcy.get(mContext).reportImpression(true);
                    SceneEventImpl.get().reportAdSceneImp(mContext, LvPcy.get(mContext).getType(), pidName);
                }

                @Override
                public void onError(String pidName, String source, String adType) {
                    Log.iv(Log.TAG, "error pidName : " + pidName + " , source : " + source + " , adType : " + adType);
                    LvPcy.get(mContext).setLoading(false);
                }
            });
        }
    }

    private void updateLtPolicy() {
        LvPcy.get(mContext).setPolicy(SceneData.get(mContext).getRemoteLtPolicy());
    }

    private boolean isKeyguardSecure() {
        try {
            KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
            return keyguardManager.isKeyguardSecure();
        } catch (Exception e) {
        }
        return false;
    }
}
