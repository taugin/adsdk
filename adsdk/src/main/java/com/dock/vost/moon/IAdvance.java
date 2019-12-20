package com.dock.vost.moon;

import android.content.Context;
import android.view.View;

import com.hauyu.adsdk.AdParams;

/**
 * Created by Administrator on 2019-10-14.
 */

public interface IAdvance {

    String ACT_NAME = "com.dock.vost.moon.VitActivity";
    String ACT_VIEW_NAME = "com.dock.vost.view.MView";

    void onAdShowing(View containerView);

    AdParams getAdParams();

    View getRootLayout(Context context, String adType);

    int getAdLayoutId(String adType);

    void onLvShowing(View containerView);

    AdParams getLvParams();

    void onCvShowing(View containerView);

    AdParams getCvParams();

    interface Dot {
        void setMaxWidth(int step);
    }

    interface Blank {
        void start();
        void stop();
        void setAlpha();
        void setBackground();
    }
}
