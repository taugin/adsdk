package com.mix.ads.core.framework;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mix.ads.MiSdk;
import com.mix.ads.MiStat;
import com.mix.ads.utils.Utils;
import com.mix.ads.log.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VersionManager {
    private static VersionManager sVersionManager;

    public static VersionManager get(Context context) {
        synchronized (VersionManager.class) {
            if (sVersionManager == null) {
                createInstance(context);
            }
        }
        return sVersionManager;
    }

    private static void createInstance(Context context) {
        synchronized (VersionManager.class) {
            if (sVersionManager == null) {
                sVersionManager = new VersionManager(context);
            }
        }
    }

    private VersionManager(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
    }

    private static final String CFG_VERSION_INFO = "cfg_version_info";
    private Context mContext;
    private VersionInfo mVersionInfo;
    private String mMD5;

    public void checkVersion(Activity activity) {
        try {
            checkVersionInternal(activity);
        } catch (Exception e) {
            Log.iv(Log.TAG_SDK, "error : " + e);
        }
    }

    private void checkVersionInternal(Activity activity) {
        if (activity == null) {
            Log.iv(Log.TAG_SDK, "activity is null");
            return;
        }
        parseVersionConfig();
        if (mVersionInfo == null) {
            Log.iv(Log.TAG_SDK, "version info is null");
            return;
        }
        if (!mVersionInfo.enable) {
            Log.iv(Log.TAG_SDK, "version enable is false");
            return;
        }
        if (TextUtils.isEmpty(mVersionInfo.title)) {
            Log.iv(Log.TAG_SDK, "version title is null");
            return;
        }
        if (TextUtils.isEmpty(mVersionInfo.desc)) {
            Log.iv(Log.TAG_SDK, "version desc is null");
            return;
        }
        if (mVersionInfo.checkClass == null || mVersionInfo.checkClass.isEmpty()) {
            Log.iv(Log.TAG_SDK, "version check class is null");
            return;
        }
        int versionCode = Utils.getVersionCode(mContext);
        if (versionCode >= mVersionInfo.versionCode) {
            Log.iv(Log.TAG_SDK, "version code is low");
            return;
        }
        String versionName = Utils.getVersionName(mContext);
        if (mVersionInfo.versions != null && !mVersionInfo.versions.contains(versionName)) {
            Log.iv(Log.TAG_SDK, "version name exclude");
            return;
        }
        String currentActivityClass = null;
        try {
            currentActivityClass = activity.getClass().getName();
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(currentActivityClass) || !mVersionInfo.checkClass.contains(currentActivityClass)) {
            Log.iv(Log.TAG_SDK, "activity class name is null or uncheck");
            return;
        }
        mVersionInfo.title = mVersionInfo.title.replace("$version", versionName);
        mVersionInfo.desc = mVersionInfo.desc.replace("$version", versionName);
        Dialog dialog = generateVersionDialog(activity, new OnVersionListener() {
            @Override
            public void onUpdateView(TextView titleView, TextView descView, TextView cancelButton, TextView okButton) {
                titleView.setText(mVersionInfo.title);
                descView.setText(Html.fromHtml(mVersionInfo.desc));
                if (!TextUtils.isEmpty(mVersionInfo.cancelButton)) {
                    cancelButton.setText(mVersionInfo.cancelButton);
                }
                if (!TextUtils.isEmpty(mVersionInfo.okButton)) {
                    okButton.setText(mVersionInfo.okButton);
                }
            }

            @Override
            public void onUpgrade() {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (!TextUtils.isEmpty(mVersionInfo.url)) {
                        intent.setData(Uri.parse(mVersionInfo.url));
                    } else if (!TextUtils.isEmpty(mVersionInfo.packageName)) {
                        intent.setData(Uri.parse("market://details?id=" + mVersionInfo.packageName));
                    } else {
                        intent.setData(Uri.parse("market://details?id=" + mContext.getPackageName()));
                    }
                    if (mVersionInfo.isPlay && Utils.isInstalled(mContext, "com.android.vending")) {
                        intent.setPackage("com.android.vending");
                    }
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    Log.iv(Log.TAG_SDK, "error : " + e);
                    MiStat.reportEvent(mContext, "report_upgrade_error", "" + e);
                }
            }
        });
        if (dialog != null) {
            dialog.setCancelable(mVersionInfo.cancelable);
            dialog.show();
        }
    }

    private Dialog generateVersionDialog(Activity activity, OnVersionListener onVersionListener) {
        LinearLayout rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        updateDialogBackground(rootLayout);
        int titleHeight = Utils.dp2px(mContext, 48);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, titleHeight);
        TextView titleView = new TextView(mContext);
        titleView.setTextColor(Color.BLACK);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleView.setTypeface(null, Typeface.BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            titleView.setElevation(1f);
        }
        rootLayout.addView(titleView, titleParams);

        ImageView lineView = new ImageView(mContext);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(-1, Utils.dp2px(mContext, 1));
        lineParams.leftMargin = Utils.dp2px(mContext, 2);
        lineParams.rightMargin = Utils.dp2px(mContext, 2);
        lineView.setBackgroundColor(Color.LTGRAY);
        rootLayout.addView(lineView, lineParams);

        LinearLayout descLayout = new LinearLayout(mContext);
        descLayout.setMinimumHeight(Utils.dp2px(mContext, 72));
        descLayout.setGravity(Gravity.CENTER);
        TextView descView = new TextView(mContext);
        descLayout.addView(descView);
        descView.setTextColor(Color.BLACK);
        descView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(-1, -2);
        descParams.topMargin = Utils.dp2px(mContext, 8);
        rootLayout.addView(descLayout, descParams);

        lineView = new ImageView(mContext);
        lineParams = new LinearLayout.LayoutParams(-1, Utils.dp2px(mContext, 1));
        lineParams.leftMargin = Utils.dp2px(mContext, 2);
        lineParams.rightMargin = Utils.dp2px(mContext, 2);
        lineParams.topMargin = Utils.dp2px(mContext, 8);
        lineView.setBackgroundColor(Color.LTGRAY);
        rootLayout.addView(lineView, lineParams);

        LinearLayout buttonLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, Utils.dp2px(mContext, 48));
        rootLayout.addView(buttonLayout, buttonParams);
        TextView cancelButton = new TextView(mContext);
        cancelButton.setText(android.R.string.cancel);
        cancelButton.setGravity(Gravity.CENTER);
        cancelButton.setTextColor(Color.GRAY);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(-1, -1);
        cancelParams.weight = 1;
        buttonLayout.addView(cancelButton, cancelParams);

        ImageView vLine = new ImageView(mContext);
        vLine.setBackgroundColor(Color.LTGRAY);
        LinearLayout.LayoutParams vLineParams = new LinearLayout.LayoutParams(Utils.dp2px(mContext, 1), -1);
        vLineParams.topMargin = Utils.dp2px(mContext, 1);
        vLineParams.bottomMargin = Utils.dp2px(mContext, 1);
        buttonLayout.addView(vLine, vLineParams);

        TextView okButton = new TextView(mContext);
        okButton.setText(android.R.string.ok);
        okButton.setGravity(Gravity.CENTER);
        okButton.setTextColor(Color.BLACK);
        okButton.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams okParams = new LinearLayout.LayoutParams(-1, -1);
        okParams.weight = 1;
        buttonLayout.addView(okButton, okParams);

        setButtonBackground(cancelButton, okButton);

        Dialog dialog = new Dialog(activity);
        dialog.setContentView(rootLayout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (onVersionListener != null) {
            onVersionListener.onUpdateView(titleView, descView, cancelButton, okButton);
        }
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (onVersionListener != null) {
                    onVersionListener.onUpgrade();
                }
            }
        });
        return dialog;
    }

    private void updateDialogBackground(View view) {
        float corner = Utils.dp2px(view.getContext(), 2);
        float[] roundArray = new float[]{corner, corner, corner, corner, corner, corner, corner, corner};
        ShapeDrawable roundBackground = new ShapeDrawable(new RoundRectShape(roundArray, (RectF) null, (float[]) null));
        roundBackground.getPaint().setColor(Color.WHITE);
        view.setBackground(roundBackground);
    }

    private void setButtonBackground(View cancelButton, View okButton) {
        try {
            float corner = Utils.dp2px(okButton.getContext(), 2);
            float[] roundArray = new float[]{corner, corner, corner, corner, corner, corner, corner, corner};
            ShapeDrawable shapePressed = new ShapeDrawable(new RoundRectShape(roundArray, (RectF) null, (float[]) null));
            shapePressed.getPaint().setColor(Color.LTGRAY);

            ShapeDrawable shapeNormal = new ShapeDrawable(new RoundRectShape(roundArray, (RectF) null, (float[]) null));
            shapeNormal.getPaint().setColor(Color.WHITE);

            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
            drawable.addState(new int[]{android.R.attr.state_enabled}, shapeNormal);
            okButton.setBackground(drawable);

            drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
            drawable.addState(new int[]{android.R.attr.state_enabled}, shapeNormal);
            cancelButton.setBackground(drawable);
        } catch (Exception e) {
            Log.iv(Log.TAG, "set bg error : " + e);
        }
    }

    private void parseVersionConfig() {
        String versionInfoString = MiSdk.get(mContext).getString(CFG_VERSION_INFO);
        if (!TextUtils.isEmpty(versionInfoString)) {
            String contentMD5 = Utils.string2MD5(versionInfoString);
            if (mVersionInfo != null && TextUtils.equals(contentMD5, mMD5)) {
                Log.iv(Log.TAG_SDK, "version config has parsed");
                return;
            }
            mMD5 = contentMD5;
            VersionInfo versionInfo = null;
            try {
                JSONObject jsonObject = new JSONObject(versionInfoString);
                versionInfo = new VersionInfo();
                versionInfo.enable = jsonObject.optBoolean("enable");
                versionInfo.isPlay = jsonObject.optBoolean("play");
                versionInfo.cancelable = jsonObject.optBoolean("cancelable", true);
                versionInfo.title = jsonObject.optString("title");
                versionInfo.desc = jsonObject.optString("desc");
                versionInfo.versionCode = jsonObject.optInt("version_code", -1);
                versionInfo.packageName = jsonObject.optString("package");
                versionInfo.okButton = jsonObject.optString("ok_button");
                versionInfo.cancelButton = jsonObject.optString("cancel_button");
                versionInfo.checkClass = parseStringList(jsonObject.optString("check_class"));
                versionInfo.url = jsonObject.optString("url");
                versionInfo.versions = parseStringList(jsonObject.optString("versions"));
            } catch (Exception e) {
                Log.iv(Log.TAG_SDK, "error : " + e);
            }
            mVersionInfo = versionInfo;
        }
    }

    private List<String> parseStringList(String str) {
        List<String> list = null;
        try {
            JSONArray jarray = new JSONArray(str);
            if (jarray != null && jarray.length() > 0) {
                list = new ArrayList<String>(jarray.length());
                for (int index = 0; index < jarray.length(); index++) {
                    String s = jarray.getString(index);
                    if (!TextUtils.isEmpty(s)) {
                        list.add(s);
                    }
                }
            }
        } catch (Exception e) {
        }
        return list;
    }

    class VersionInfo {
        public boolean enable;
        public boolean isPlay;
        public boolean cancelable = true;
        public String title;
        public String desc;
        public String packageName;
        public String okButton;
        public String cancelButton;
        public List<String> checkClass;
        public String url;
        public int versionCode = -1;
        public List<String> versions;

        @Override
        public String toString() {
            return "VersionInfo{" +
                    "enable=" + enable +
                    ", playPrefer=" + isPlay +
                    ", title='" + title + '\'' +
                    ", desc='" + desc + '\'' +
                    ", checkClass=" + checkClass +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    interface OnVersionListener {
        void onUpdateView(TextView titleView, TextView descView, TextView cancelButton, TextView okButton);

        void onUpgrade();
    }
}
