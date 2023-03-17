package com.android.support;

import android.content.Context;
import android.view.View;

import com.rabbit.adsdk.AdParams;

/**
 * Created by Administrator on 2019-10-14.
 */

public interface IAdvance {
    String ACT_VIEW_NAME = MaskView.class.getName();

    AdParams getAdParams(String adType);

    View getRootLayout(Context context, String adType);

    int getAdLayoutId(String adType);
}
