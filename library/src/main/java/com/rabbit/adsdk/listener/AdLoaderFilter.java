package com.rabbit.adsdk.listener;

public interface AdLoaderFilter {
    boolean doFilter(String pidName, String sdk, String type);
}
