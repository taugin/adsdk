package com.tiger.adsdk.listener;

/**
 * 广告加载过滤器，返回true表示不加载此广告，返回false表示可以加载广告
 */
public interface OnAdFilterListener {
    /**
     * 返回true标识，过滤此广告，返回false标识允许此广告
     *
     * @param placeName
     * @param sdk
     * @param type
     * @return
     */
    boolean doFilter(String placeName, String sdk, String type);
}
