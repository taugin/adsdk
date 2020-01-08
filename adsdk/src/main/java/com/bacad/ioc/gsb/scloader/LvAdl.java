package com.bacad.ioc.gsb.scloader;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.bacad.ioc.gsb.common.Bldr;
import com.bacad.ioc.gsb.common.CSvr;
import com.bacad.ioc.gsb.data.SceneData;
import com.bacad.ioc.gsb.scpolicy.LvPcy;
import com.dock.vist.sun.IAdvance;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.util.Locale;

/**
 * Created by Administrator on 2019/8/18.
 */

public class LvAdl extends Bldr {

    private static final int MSG_SHOW_LOCKSCREEN = 123456789;
    private static final int DELAY = 5000;
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
        showLockScreen();
    }

    @Override
    public void onUserPresent(Context context) {
        showLockScreen();
    }

    private void showLockScreen() {
        if (mHandler != null) {
            if (!mHandler.hasMessages(MSG_SHOW_LOCKSCREEN)) {
                showLs();
                mHandler.sendEmptyMessageDelayed(MSG_SHOW_LOCKSCREEN, DELAY);
            } else {
                mHandler.removeMessages(MSG_SHOW_LOCKSCREEN);
            }
        }
    }


    private void showLs() {
        updateLtPolicy();
        if (!LvPcy.get(mContext).isLtAllowed()) {
            return;
        }
        String placeName = getPlaceNameAdv();
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
                ComponentName cmp = new ComponentName(mContext, IAdvance.ACT_NAME);
                intent.setComponent(cmp);
            }
            intent.putExtra(Intent.EXTRA_TITLE, placeName);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(Intent.EXTRA_REPLACING, pType);
            try {
                PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntent.send();
            } catch (Exception e) {
                try {
                    mContext.startActivity(intent);
                    LvPcy.get(mContext).reportImpression(true);
                } catch (Exception e1) {
                }
            }
        } catch (Exception e) {
            Log.v(Log.TAG, "error : " + e);
        }
    }

    private void updateLtPolicy() {
        LvPcy.get(mContext).setPolicy(SceneData.get(mContext).getRemoteLtPolicy());
    }
}
