package com.inner.adaggs.request;


/**
 * Created by Administrator on 2018/2/12.
 */

public interface IDataRequest {
    public void request();
    public void setOnDataListener(OnDataListener l);
    public String getString(String key);
}
