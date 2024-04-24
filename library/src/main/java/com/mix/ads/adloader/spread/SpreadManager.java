package com.mix.ads.adloader.spread;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.view.View;

import com.mix.ads.MiStat;
import com.mix.ads.adloader.listener.ISdkLoader;
import com.mix.ads.core.db.DBManager;
import com.mix.ads.core.framework.ActivityMonitor;
import com.mix.ads.data.config.SpreadConfig;
import com.mix.ads.log.Log;
import com.mix.ads.utils.Utils;

import java.util.Locale;

public class SpreadManager {

    public static final String AD_SPREAD_LIST = "cfg_spread_list";

    private static SpreadManager sSpreadManager;

    public static SpreadManager get(Context context) {
        synchronized (SpreadManager.class) {
            if (sSpreadManager == null) {
                createInstance(context);
            }
        }
        return sSpreadManager;
    }

    private static void createInstance(Context context) {
        synchronized (SpreadManager.class) {
            if (sSpreadManager == null) {
                sSpreadManager = new SpreadManager(context);
            }
        }
    }

    private SpreadManager(Context context) {
        mContext = context;
        register(context);
    }

    private Context mContext;

    public void insertOrUpdateClick(String bundle, long clickTime) {
        DBManager.get(mContext).insertOrUpdateClick(bundle, clickTime);
    }

    public String generateReferrer(Context context, String campaign) {
        String packageName = context.getPackageName();
        return String.format(Locale.ENGLISH, "referrer=utm_source%%3D%s%%26utm_medium%%3Dcpc%%26utm_campaign%%3D%s", packageName, campaign);
    }

    private static void reportAdSpreadInstalled(Context context, String packageName) {
        try {
            MiStat.reportEvent(context, "ad_spread_installed", packageName);
        } catch (Exception e) {
        }
    }

    private void register(Context context) {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addDataScheme("package");
            context.registerReceiver(sBroadcastReceiver, filter);
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private static String parsePackageName(Intent intent) {
        try {
            String data = intent.getDataString();
            return data.substring("package:".length());
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    private static BroadcastReceiver sBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                return;
            }
            try {
                String action = intent.getAction();
                Log.iv(Log.TAG, "action : " + action + " , data : " + intent.getDataString());
                if (TextUtils.equals(action, Intent.ACTION_PACKAGE_ADDED)) {
                    String packageName = parsePackageName(intent);
                    DBManager.SpreadClickInfo spreadClickInfo = DBManager.get(context).queryClickSpread(packageName);
                    if (spreadClickInfo != null) {
                        int _id = spreadClickInfo._id;
                        Log.iv(Log.TAG, "install package name : " + packageName + " , _id : " + _id);
                        if (_id >= 0) {
                            DBManager.get(context).updateInstallTime(_id, System.currentTimeMillis(), spreadClickInfo.installCount + 1);
                            reportAdSpreadInstalled(context, packageName);
                        }
                    }
                }
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
            }
        }
    };

    private boolean checkSpConfig(SpreadConfig spreadConfig) {
        return (spreadConfig != null
                && !TextUtils.isEmpty(spreadConfig.getIcon())
                && !TextUtils.isEmpty(spreadConfig.getTitle())
                && !TextUtils.isEmpty(spreadConfig.getBundle())
                && !TextUtils.isEmpty(spreadConfig.getCta())
                && !Utils.isInstalled(mContext, spreadConfig.getBundle())
                && !TextUtils.equals(spreadConfig.getBundle(), mContext.getPackageName()));
    }

    public boolean showFullScreenAds(SpreadConfig spreadConfig, SpLoader.ClickClass clickClass, ISdkLoader iSdkLoader) {
        Activity activity = ActivityMonitor.get(mContext).getTopActivity();
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        if (!checkSpConfig(spreadConfig)) {
            return false;
        }
        try {
            final Dialog dialog = new Dialog(activity, android.R.style.Theme_Material_Light_NoActionBar);
            View viewRoot = BaseIntView.generate(activity)
                    .setActionClickListener(clickClass)
                    .setCloseClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            } catch (Exception e) {
                            }
                        }
                    })
                    .render(spreadConfig);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    try {
                        if (iSdkLoader != null) {
                            iSdkLoader.notifyAdViewUIDismiss();
                        }
                    } catch (Exception e) {
                    }
                }
            });
            dialog.setContentView(viewRoot);
            dialog.setCancelable(false);
            dialog.show();
            return true;
        } catch (Exception e) {
        }
        return false;
    }
}
