package com.hauyu.adsdk.policy;

import android.content.Context;
import android.text.TextUtils;

import com.hauyu.adsdk.config.AtConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-8-10.
 */

public class AtPolicy extends BasePolicy {
    private static final List<String> WHITE_LIST;

    static {
        WHITE_LIST = new ArrayList<String>();
        WHITE_LIST.add("com.android.launcher");
        WHITE_LIST.add("com.google.android.talk");
        WHITE_LIST.add("com.google.android.apps.plus");
        WHITE_LIST.add("com.android.providers.downloads.ui");
        WHITE_LIST.add("com.android.music");
        WHITE_LIST.add("com.android.browser");
        WHITE_LIST.add("com.android.settings");
        WHITE_LIST.add("com.android.camera2");
        WHITE_LIST.add("com.android.deskclock");
        WHITE_LIST.add("com.android.calculator2");
        WHITE_LIST.add("com.android.chrome");
        WHITE_LIST.add("com.android.contacts");
        WHITE_LIST.add("com.android.dialer");
        WHITE_LIST.add("com.android.email");
        WHITE_LIST.add("com.android.gallery3d");
        WHITE_LIST.add("com.android.mms");
        WHITE_LIST.add("com.android.vending");
        WHITE_LIST.add("com.facebook.katana");
        WHITE_LIST.add("com.facebook.orca");
        WHITE_LIST.add("com.google.android.apps.books");
        WHITE_LIST.add("com.google.android.apps.docs");
        WHITE_LIST.add("com.google.android.apps.fireball");
        WHITE_LIST.add("com.google.android.apps.magazines");
        WHITE_LIST.add("com.google.android.apps.maps");
        WHITE_LIST.add("com.google.android.apps.photos");
        WHITE_LIST.add("com.google.android.apps.playconsole");
        WHITE_LIST.add("com.google.android.apps.translate");
        WHITE_LIST.add("com.google.android.calendar");
        WHITE_LIST.add("com.google.android.gms");
        WHITE_LIST.add("com.google.android.gm");
        WHITE_LIST.add("com.google.android.googlequicksearchbox");
        WHITE_LIST.add("com.google.android.music");
        WHITE_LIST.add("com.google.android.play.games");
        WHITE_LIST.add("com.google.android.videos");
        WHITE_LIST.add("com.google.android.youtube");
        WHITE_LIST.add("com.imo.android.imoim");
        WHITE_LIST.add("com.instagram.android");
        WHITE_LIST.add("com.whatsapp");
    }

    private static AtPolicy sAtPolicy;

    public static AtPolicy get(Context context) {
        synchronized (AtPolicy.class) {
            if (sAtPolicy == null) {
                createInstance(context);
            }
        }
        return sAtPolicy;
    }

    private static void createInstance(Context context) {
        synchronized (AtPolicy.class) {
            if (sAtPolicy == null) {
                sAtPolicy = new AtPolicy(context);
            }
        }
    }

    private AtPolicy(Context context) {
        super(context, "at");
    }

    private AtConfig mAtConfig;

    public void init() {
    }

    public void setPolicy(AtConfig atConfig) {
        super.setPolicy(atConfig);
        mAtConfig = atConfig;
        if (mAtConfig != null && (mAtConfig.getExcludes() == null || mAtConfig.getExcludes().isEmpty())) {
            mAtConfig.setExcludes(WHITE_LIST);
        }
    }

    public boolean isAtAllowed() {
        Log.iv(Log.TAG, "at : " + mAtConfig);
        if (!checkBaseConfig()) {
            return false;
        }

        if (isTopApp()) {
            Log.iv(Log.TAG, "app is on the top");
            return false;
        }

        if (Utils.isScreenLocked(mContext)) {
            Log.iv(Log.TAG, "screen is locked");
            return false;
        }

        if (!Utils.isScreenOn(mContext)) {
            Log.iv(Log.TAG, "screen is not on");
            return false;
        }
        return true;
    }

    public boolean isInWhiteList(String pkgname, String className) {
        // exclude launcher
        if (pkgname != null && pkgname.contains("launcher")
                || className != null && className.contains("launcher")) {
            Log.iv(Log.TAG, "exclude launcher");
            return true;
        }
        if (mAtConfig != null && mAtConfig.getExcludes() != null && mAtConfig.getExcludes().contains(pkgname)) {
            Log.iv(Log.TAG, "white name " + pkgname);
            return true;
        }
        if (mContext != null && TextUtils.equals(pkgname, mContext.getPackageName())) {
            Log.iv(Log.TAG, "exclude self");
            return true;
        }
        return false;
    }

    public boolean isShowOnFirstPage() {
        if (mAtConfig != null) {
            return mAtConfig.isShowOnFirstPage();
        }
        return false;
    }
}
