package com.inner.adaggs.listener;

/**
 * Created by Administrator on 2018/2/11.
 */

public interface OnAdAggsListener {
    public void onLoaded(String pidName, String source, String adType);
    public void onShow(String pidName, String source, String adType);
    public void onClick(String pidName, String source, String adType);
    public void onDismiss(String pidName, String source, String adType);
    public void onError(String pidName, String source, String adType);
}
