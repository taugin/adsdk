package com.inner.adaggs.listener;

/**
 * Created by Administrator on 2018/2/11.
 */

public interface OnAdAggsListener {
    public void onLoaded(String source, String adType);
    public void onShow(String source, String adType);
    public void onClick(String source, String adType);
    public void onDismiss(String source, String adType);
    public void onError(String source, String adType);
}
