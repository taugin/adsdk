package com.hauyu.adsdk.listener;


import com.hauyu.adsdk.common.BaseConfig;

import org.json.JSONObject;

/**
 * Created by Administrator on 2019-4-9.
 */

public interface IParseListener {
    void parse(BaseConfig baseConfig, JSONObject jobj);
}
