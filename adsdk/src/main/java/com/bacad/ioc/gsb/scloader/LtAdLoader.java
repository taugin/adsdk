package com.bacad.ioc.gsb.scloader;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.bacad.ioc.gsb.data.SceneData;
import com.bacad.ioc.gsb.scpolicy.LtPolicy;
import com.gekes.fvs.tdsvap.GFAPSD;
import com.hauyu.adsdk.AdSdk;
import com.bacad.ioc.gsb.common.BaseLoader;
import com.hauyu.adsdk.core.AdReceiver;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

/**
 * Created by Administrator on 2019/8/18.
 */

public class LtAdLoader extends BaseLoader {

    private static final int MSG_SHOW_LOCKSCREEN = 123456789;
    private static final int DELAY = 5000;
    private static LtAdLoader sLtAdLoader;

    private Context mContext;
    private AdSdk mAdSdk;
    private Handler mHandler = new Handler();

    private LtAdLoader(Context context) {
        mContext = context.getApplicationContext();
        AdReceiver.get(context).registerTriggerListener(this);
    }

    public static LtAdLoader get(Context context) {
        if (sLtAdLoader == null) {
            create(context);
        }
        return sLtAdLoader;
    }

    private static void create(Context context) {
        synchronized (LtAdLoader.class) {
            if (sLtAdLoader == null) {
                sLtAdLoader = new LtAdLoader(context);
            }
        }
    }

    public void init() {
        mAdSdk = AdSdk.get(mContext);
        if (mAdSdk == null) {
            return;
        }
        LtPolicy.get(mContext).init();
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
        if (!LtPolicy.get(mContext).isLtAllowed()) {
            return;
        }
        try {
            Intent intent = Utils.getIntentByAction(mContext, mContext.getPackageName() + ".action.LSPICKER", false, false);
            if (intent == null) {
                intent = new Intent(mContext, GFAPSD.class);
            }
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
            LtPolicy.get(mContext).reportShowing(true);
        } catch (Exception e) {
            Log.v(Log.TAG, "error : " + e);
        }
    }

    private void updateLtPolicy() {
        LtPolicy.get(mContext).setPolicy(SceneData.get(mContext).getRemoteLtPolicy());
    }
}
