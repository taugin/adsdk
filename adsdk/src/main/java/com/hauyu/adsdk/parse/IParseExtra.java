package com.hauyu.adsdk.parse;


import com.hauyu.adsdk.config.BaseConfig;

import org.json.JSONObject;

/**
 * Created by Administrator on 2019-4-9.
 */

public interface IParseExtra {
    void parse(BaseConfig baseConfig, JSONObject jobj);
}
