package com.hauyu.adsdk.core;


/**
 * Created by Administrator on 2018/2/12.
 */

public interface IDataRequest {
    void request();
    void refresh();
    void setUrl(String address);
    String getString(String key);
}
