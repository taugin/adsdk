package com.inner.adsdk.http;

/**
 * Created by Administrator on 2018/1/17.
 */

public interface OnCallback {
    public void onSuccess(String content);
    public void onFailure(int code, String error);
}
