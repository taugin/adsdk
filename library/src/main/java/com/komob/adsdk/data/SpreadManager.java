package com.komob.adsdk.data;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.komob.adsdk.InternalStat;
import com.komob.adsdk.adloader.listener.ISdkLoader;
import com.komob.adsdk.core.db.DBManager;
import com.komob.adsdk.core.framework.ActivityMonitor;
import com.komob.adsdk.core.framework.Params;
import com.komob.adsdk.data.config.SpreadConfig;
import com.komob.adsdk.http.Http;
import com.komob.adsdk.http.OnImageCallback;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.utils.Utils;
import com.komob.adsdk.utils.VUIHelper;
import com.komob.api.RFileConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

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
    private Random mRandom = new Random(System.currentTimeMillis());

    private List<SpreadConfig> getSpreadList() {
        List<SpreadConfig> list = DataManager.get(mContext).getSpreadList();
        if (list == null || list.isEmpty()) {
            return null;
        }
        List<SpreadConfig> checkedList = new ArrayList<>();
        for (SpreadConfig spreadConfig : list) {
            if (spreadConfig != null
                    && !TextUtils.isEmpty(spreadConfig.getIcon())
                    && !TextUtils.isEmpty(spreadConfig.getTitle())
                    && !TextUtils.isEmpty(spreadConfig.getBundle())
                    && !TextUtils.isEmpty(spreadConfig.getCta())
                    && !Utils.isInstalled(mContext, spreadConfig.getBundle())
                    && !TextUtils.equals(spreadConfig.getBundle(), mContext.getPackageName())) {
                checkedList.add(spreadConfig);
            }
        }
        return checkedList;
    }

    public boolean hasSpreadApp() {
        List<SpreadConfig> list = getSpreadList();
        return list != null && !list.isEmpty();
    }

    public int getSpreadAppCount() {
        List<SpreadConfig> list = getSpreadList();
        return list != null ? list.size() : 0;
    }

    public void showSpreadUI() {
        List<SpreadConfig> list = getSpreadList();
        if (list == null || list.isEmpty()) {
            return;
        }

        Activity activity = ActivityMonitor.get(mContext).getTopActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        boolean useSingleColumn = mRandom != null ? mRandom.nextBoolean() : new Random().nextBoolean();
        View view = LayoutInflater.from(mContext).inflate(RFileConfig.kom_layout_grid, null);
        TextView titleView = view.findViewById(RFileConfig.kom_title_view);
        titleView.setText(getSponsoredText(mContext));
        GridView gridView = view.findViewById(RFileConfig.kom_spread_grid);
        gridView.setNumColumns(useSingleColumn ? 1 : 3);
        ArrayAdapter<SpreadConfig> adapter = new ArrayAdapter<SpreadConfig>(mContext, 0, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder = null;
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(RFileConfig.kom_layout_item, null);
                    viewHolder = new ViewHolder();
                    if (useSingleColumn) {
                        convertView.findViewById(RFileConfig.kom_group_single).setVisibility(View.VISIBLE);
                        convertView.findViewById(RFileConfig.kom_group_multiple).setVisibility(View.GONE);
                        viewHolder.iconViewSingle = convertView.findViewById(RFileConfig.kom_app_icon_single);
                        viewHolder.nameViewSingle = convertView.findViewById(RFileConfig.kom_app_name_single);
                        viewHolder.detailViewSingle = convertView.findViewById(RFileConfig.kom_app_detail_single);
                        viewHolder.actionViewSingle = convertView.findViewById(RFileConfig.kom_action_view_single);
                    } else {
                        convertView.findViewById(RFileConfig.kom_group_single).setVisibility(View.GONE);
                        convertView.findViewById(RFileConfig.kom_group_multiple).setVisibility(View.VISIBLE);
                        viewHolder.iconViewMultiple = convertView.findViewById(RFileConfig.kom_app_icon_multiple);
                        viewHolder.nameViewMultiple = convertView.findViewById(RFileConfig.kom_app_name_multiple);
                        viewHolder.actionViewMultiple = convertView.findViewById(RFileConfig.kom_action_view_multiple);
                    }
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                final SpreadConfig spreadConfig = getItem(position);
                if (useSingleColumn) {
                    viewHolder.actionViewSingle.setTag(spreadConfig);
                    viewHolder.actionViewSingle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            clickSponsoredApp(mContext, spreadConfig);
                        }
                    });
                    if (spreadConfig != null) {
                        viewHolder.actionViewSingle.setText(spreadConfig.getCta());
                        viewHolder.nameViewSingle.setText(spreadConfig.getTitle());
                        viewHolder.detailViewSingle.setText(spreadConfig.getDetail());
                        loadAndShowImage(viewHolder.iconViewSingle, spreadConfig.getIcon());
                    }
                } else {
                    viewHolder.actionViewMultiple.setTag(spreadConfig);
                    viewHolder.actionViewMultiple.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            clickSponsoredApp(mContext, spreadConfig);
                        }
                    });
                    if (spreadConfig != null) {
                        viewHolder.actionViewMultiple.setText(spreadConfig.getCta());
                        viewHolder.nameViewMultiple.setText(spreadConfig.getTitle());
                        loadAndShowImage(viewHolder.iconViewMultiple, spreadConfig.getIcon());
                    }
                }
                return convertView;
            }
        };
        gridView.setAdapter(adapter);
        Dialog dialog = new Dialog(activity, android.R.style.Theme_Material_Light_NoActionBar);
        dialog.setContentView(view);
        dialog.show();
        View backView = view.findViewById(RFileConfig.kom_arrow_back);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    private void clickSponsoredApp(Context context, SpreadConfig spreadConfig) {
        if (spreadConfig != null) {
            String url = spreadConfig.getLinkUrl();
            String packageName = spreadConfig.getBundle();
            boolean isPlay = spreadConfig.isPlay();
            String referrer = null;
            try {
                referrer = generateReferrer(context, "sponsored");
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (TextUtils.isEmpty(url)) {
                url = "market://details?id=" + packageName;
                if (!TextUtils.isEmpty(referrer)) {
                    url = url + "&" + referrer;
                }
                if (isPlay && Utils.isInstalled(context, "com.android.vending")) {
                    intent.setPackage("com.android.vending");
                }
            }
            Log.iv(Log.TAG, "spread url : " + url);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
                SpreadManager.get(mContext).insertOrUpdateClick(packageName, System.currentTimeMillis());
            } catch (Exception e) {
                Log.iv(Log.TAG, "error : " + e);
            }
            InternalStat.reportEvent(mContext, "ad_sponsored_click", packageName);
        }
    }

    public void insertOrUpdateClick(String bundle, long clickTime) {
        DBManager.get(mContext).insertOrUpdateClick(bundle, clickTime);
    }

    public String generateReferrer(Context context, String campaign) {
        String packageName = context.getPackageName();
        String gclid = Utils.string2MD5(UUID.randomUUID().toString());
        return String.format(Locale.ENGLISH, "referrer=utm_source%%3D%s%%26utm_medium%%3Dcpc%%26utm_campaign%%3D%s%%26gclid%%3D%s", packageName, campaign, gclid);
    }

    private void loadAndShowImage(final ImageView imageView, String url) {
        try {
            Http.get(imageView.getContext()).loadImage(url, ImageView.ScaleType.CENTER_CROP, new OnImageCallback() {

                @Override
                public void onSuccess(Bitmap bitmap) {
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }

                @Override
                public void onFailure(int code, String error) {
                    Log.iv(Log.TAG, "code : " + code + " , error : " + error);
                }
            });
        } catch (Exception e) {
        }
    }

    protected String getSponsoredText(Context context) {
        Locale locale = getLocale(context);
        if (locale != null) {
            if (isLocaleEquals(Locale.SIMPLIFIED_CHINESE, locale)) {
                return "应用推广";
            }
            if (isLocaleEquals(Locale.TRADITIONAL_CHINESE, locale)) {
                return "應用推廣";
            }
            if (isLocaleEquals(Locale.JAPANESE, locale) || isLocaleEquals(Locale.JAPAN, locale)) {
                return "应用推广";
            }
            if (isLocaleEquals(Locale.KOREA, locale) || isLocaleEquals(Locale.KOREAN, locale)) {
                return "应용推广";
            }
        }
        return "Sponsored Apps";
    }

    private boolean isLocaleEquals(Locale locale1, Locale locale2) {
        if (locale1 == null || locale2 == null) {
            return false;
        }
        String language1 = locale1.getLanguage();
        String language2 = locale2.getLanguage();
        String country1 = locale1.getCountry();
        String country2 = locale2.getCountry();
        if (language1 == null || language2 == null || country1 == null || country2 == null) {
            return false;
        }
        return language1.equalsIgnoreCase(language2) && country1.equalsIgnoreCase(country2);
    }

    protected Locale getLocale(Context context) {
        Locale locale = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
        } catch (Exception e) {
        }
        return locale;
    }

    private static void reportAdSpreadInstalled(Context context, String packageName) {
        try {
            InternalStat.reportEvent(context, "ad_spread_installed", packageName);
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

    class ViewHolder {
        ImageView iconViewMultiple;
        TextView nameViewMultiple;
        TextView actionViewMultiple;

        ImageView iconViewSingle;
        TextView nameViewSingle;
        TextView detailViewSingle;
        TextView actionViewSingle;
    }

    public void showFullScreenAds(ISdkLoader iSdkLoader, Params params) {
        Activity activity = ActivityMonitor.get(mContext).getTopActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        try {
            final Dialog dialog = new Dialog(activity, android.R.style.Theme_Material_Light_NoActionBar);
            VUIHelper vuiHelper = new VUIHelper();
            View adRootView = vuiHelper.generateNativeView(mContext, iSdkLoader, params, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    } catch (Exception e) {
                    }
                }
            });
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
            dialog.setContentView(adRootView);
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception e) {
        }
    }
}
