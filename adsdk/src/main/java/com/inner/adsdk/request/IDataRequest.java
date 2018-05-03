package com.inner.adsdk.request;


/**
 * Created by Administrator on 2018/2/12.
 */

public interface IDataRequest {
    public void request();
    public void refresh();
    public void setAddress(String address);
    public String getString(String key);
}
