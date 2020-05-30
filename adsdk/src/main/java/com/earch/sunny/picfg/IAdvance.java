package com.earch.sunny.picfg;

import android.content.Context;
import android.view.View;

import com.hauyu.adsdk.AdParams;

/**
 * Created by Administrator on 2019-10-14.
 */

public interface IAdvance {

    String ACT_VIEW_NAME = "com.earch.sunny.view.MView";

    void onSceneImp(String adType, View containerView);

    AdParams getAdParams(String adType);

    View getRootLayout(Context context, String adType);

    int getAdLayoutId(String adType);

    interface Dot {
        void setMaxWidth(int step);
    }

    interface IBlank {
        void begin();
        void end();
        void updateTransparent();
        void setBg();
    }
}
