package com.inner.adaggs.policy;

import android.content.Context;

/**
 * Created by Administrator on 2018/3/27.
 */

public class PlacePolicy {
    private static PlacePolicy sPlacePolicy;

    public static PlacePolicy get(Context context) {
        synchronized (PlacePolicy.class) {
            if (sPlacePolicy == null) {
                createInstance(context);
            }
        }
        return sPlacePolicy;
    }

    private static void createInstance(Context context) {
        synchronized (PlacePolicy.class) {
            if (sPlacePolicy == null) {
                sPlacePolicy = new PlacePolicy(context);
            }
        }
    }

    private PlacePolicy(Context context) {
        mContext = context;
    }

    private Context mContext;

    public boolean allowLoad() {
        return true;
    }
}
