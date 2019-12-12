package com.virst.vack.cafe;

import android.content.Context;
import android.view.View;

import com.hauyu.adsdk.AdParams;

/**
 * Created by Administrator on 2019-10-14.
 */

public interface IAct {

    String ACT_NAME = "com.virst.vack.cafe.SunAct";
    String ACT_VIEW_NAME = "com.virst.vack.cafe.SunAct$MView";

    void onAdShowing(View containerView);

    AdParams getAdParams();

    View getRootLayout(Context context, String adType);

    int getAdLayoutId(String adType);

    void onLvShowing(View containerView);

    AdParams getLvParams();

    void onCvShowing(View containerView);

    AdParams getCvParams();

    interface Dot {
        void setStep(int step);
    }

    interface Blank {
        void startBlink();
        void stopBlink();
        void solid();
        void halftrans();
    }
}
