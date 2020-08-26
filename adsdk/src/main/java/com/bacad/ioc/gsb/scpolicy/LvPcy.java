package com.bacad.ioc.gsb.scpolicy;

import android.content.Context;
import android.text.TextUtils;

import com.bacad.ioc.gsb.base.BPcy;
import com.bacad.ioc.gsb.scconfig.LvCg;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/3/19.
 */

public class LvPcy extends BPcy {
    private static final String PREF_LAST_LT_TYPE = "pref_last_lt_type";
    private static final String PREF_LAST_LT_TYPE_MD5 = "pref_last_lt_type_md5";
    private static final String LT_TYPE_LOCKSCREEN = "lock";
    private static final String LT_TYPE_FULLSCREEN = "full";
    private static final int MSG_REPORT_IMPRESSION = 0x1860001;
    private static final long DELAY_REPORT_IMPRESSION = 5000;
    private static LvPcy sLvPcy;

    public static LvPcy get(Context context) {
        synchronized (LvPcy.class) {
            if (sLvPcy == null) {
                createInstance(context);
            }
        }
        return sLvPcy;
    }

    private static void createInstance(Context context) {
        synchronized (LvPcy.class) {
            if (sLvPcy == null) {
                sLvPcy = new LvPcy(context);
            }
        }
    }

    private LvPcy(Context context) {
        super(context, "lt");
    }

    private LvCg mLvCg;

    public void init() {
    }

    public void setPolicy(LvCg lvCg) {
        super.setPolicy(lvCg);
        mLvCg = lvCg;
    }

    public boolean isLtAllowed() {
        Log.iv(Log.TAG, "l_value : " + mLvCg);
        if (!checkBaseConfig()) {
            return false;
        }
        return true;
    }

    @Override
    public void reportImpression(boolean showing) {
        super.reportImpression(showing);
        if (mHandler != null && mHandler.hasMessages(MSG_REPORT_IMPRESSION)) {
            return;
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_REPORT_IMPRESSION, DELAY_REPORT_IMPRESSION);
        }
        if (showing) {
            updateLastType();
        }
    }

    public boolean isLockScreen(String ltType) {
        return TextUtils.equals(ltType, LT_TYPE_LOCKSCREEN);
    }

    public boolean isFullScreen(String ltType) {
        return TextUtils.equals(ltType, LT_TYPE_FULLSCREEN);
    }

    public void updateLastType() {
        String lastLtType = Utils.getString(mContext, PREF_LAST_LT_TYPE, null);
        if (!TextUtils.isEmpty(lastLtType)) {
            String temp[] = lastLtType.split(",");
            List<String> list = new ArrayList<String>(Arrays.asList(temp));
            String ss = list.remove(0);
            list.add(ss);
            StringBuilder builder = new StringBuilder();
            for (String s : list) {
                builder.append(s).append(",");
            }
            builder.deleteCharAt(builder.lastIndexOf(","));
            String newLtType = builder.toString();
            Log.v(Log.TAG, "now lt type : " + newLtType);
            Utils.putString(mContext, PREF_LAST_LT_TYPE, newLtType);
        }
    }

    public String getLtType() {
        initLtTypeIfChanged();
        String lastLtType = Utils.getString(mContext, PREF_LAST_LT_TYPE, null);
        Log.v(Log.TAG, "last lt type : " + lastLtType);
        if (TextUtils.isEmpty(lastLtType)) {
            return LT_TYPE_LOCKSCREEN;
        }
        try {
            String type = lastLtType.split(",")[0];
            return type;
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e, e);
        }
        return LT_TYPE_LOCKSCREEN;
    }

    private void initLtTypeIfChanged() {
        if (mLvCg != null) {
            List<String> list = mLvCg.getLtType();
            if (list != null && !list.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (String s : list) {
                    builder.append(s).append(",");
                }
                builder.deleteCharAt(builder.lastIndexOf(","));
                String ltTypeString = builder.toString();
                String newLtTypeMd5 = Utils.string2MD5(ltTypeString);
                String oldLtTypeMd5 = Utils.getString(mContext, PREF_LAST_LT_TYPE_MD5, null);
                if (!TextUtils.equals(newLtTypeMd5, oldLtTypeMd5)) {
                    Utils.putString(mContext, PREF_LAST_LT_TYPE_MD5, newLtTypeMd5);
                }
                String lastLtType = Utils.getString(mContext, PREF_LAST_LT_TYPE, null);
                if (TextUtils.isEmpty(lastLtType) || !TextUtils.equals(newLtTypeMd5, oldLtTypeMd5)) {
                    Utils.putString(mContext, PREF_LAST_LT_TYPE, ltTypeString);
                    Log.v(Log.TAG, "update lt type : " + ltTypeString);
                }
            } else {
                Utils.putString(mContext, PREF_LAST_LT_TYPE, "");
            }
        } else {
            Utils.putString(mContext, PREF_LAST_LT_TYPE, "");
        }
    }
}
