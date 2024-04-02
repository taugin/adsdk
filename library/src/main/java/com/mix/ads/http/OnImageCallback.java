package com.mix.ads.http;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2018/1/17.
 */

public interface OnImageCallback extends OnCallback {
    public void onSuccess(Bitmap bitmap);
}
