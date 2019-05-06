package com.inner.adsdk.listener;

import com.inner.adsdk.common.BaseConfig;

import org.json.JSONObject;

/**
 * Created by Administrator on 2019-4-9.
 */

public interface IParseListener {
    void parse(BaseConfig baseConfig, JSONObject jobj);
}
