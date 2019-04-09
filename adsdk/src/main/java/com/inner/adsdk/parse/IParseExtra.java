package com.inner.adsdk.parse;

import com.inner.adsdk.config.BaseConfig;

import org.json.JSONObject;

/**
 * Created by Administrator on 2019-4-9.
 */

public interface IParseExtra {
    void parse(BaseConfig baseConfig, JSONObject jobj);
}
