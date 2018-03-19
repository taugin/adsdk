package com.inner.adaggs.stat;

import android.content.Context;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IStat {
    public void reportAdmobBannerRequest(Context context, String pidName, Object extra);
    public void reportAdmobBannerLoaded(Context context, String pidName, Object extra);
    public void reportAdmobBannerShow(Context context, String pidName, Object extra);
    public void reportAdmobBannerClick(Context context, String pidName, Object extra);

    public void reportAdmobInterstitialRequest(Context context, String pidName, Object extra);
    public void reportAdmobInterstitialLoaded(Context context, String pidName, Object extra);
    public void reportAdmobInterstitialShow(Context context, String pidName, Object extra);
    public void reportAdmobInterstitialClick(Context context, String pidName, Object extra);

    public void reportAdxBannerRequest(Context context, String pidName, Object extra);
    public void reportAdxBannerLoaded(Context context, String pidName, Object extra);
    public void reportAdxBannerShow(Context context, String pidName, Object extra);
    public void reportAdxBannerClick(Context context, String pidName, Object extra);

    public void reportAdxInterstitialRequest(Context context, String pidName, Object extra);
    public void reportAdxInterstitialLoaded(Context context, String pidName, Object extra);
    public void reportAdxInterstitialShow(Context context, String pidName, Object extra);
    public void reportAdxInterstitialClick(Context context, String pidName, Object extra);

    public void reportFBInterstitialRequest(Context context, String pidName, Object extra);
    public void reportFBInterstitialLoaded(Context context, String pidName, Object extra);
    public void reportFBInterstitialShow(Context context, String pidName, Object extra);
    public void reportFBInterstitialClick(Context context, String pidName, Object extra);

    public void reportFBNativeRequest(Context context, String pidName, Object extra);
    public void reportFBNativeLoaded(Context context, String pidName, Object extra);
    public void reportFBNativeShow(Context context, String pidName, Object extra);
    public void reportFBNativeClick(Context context, String pidName, Object extra);
}
