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

    void onSceneShowing(String adType, View containerView);

    AdParams getAdParams(String adType);

    View getRootLayout(Context context, String adType);

    int getAdLayoutId(String adType);

    interface Dot {
        void setMaxWidth(int step);
    }

    interface Blank {
        void startProcess();
        void stopProcess();
        void setAlpha();
        void setBackground();
    }
}
