package com.hauyu.adsdk.core;

import com.hauyu.adsdk.data.IDataRequest;

/**
 * Created by Administrator on 2019-8-16.
 */

public class BaseRequest implements IDataRequest {
    @Override
    public void request() {
    }

    @Override
    public void refresh() {
    }

    @Override
    public void setUrl(String address) {
    }

    @Override
    public String getString(String key) {
        return null;
    }
}
