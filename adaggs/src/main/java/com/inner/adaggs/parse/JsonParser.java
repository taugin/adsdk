package com.inner.adaggs.parse;

import com.inner.adaggs.config.AdInners;
import com.inner.adaggs.config.AdPlaceConfig;
import com.inner.adaggs.config.PidConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public class JsonParser implements IParser {
    @Override
    public AdInners parse(String content) {
        AdInners inners = new AdInners();
        AdPlaceConfig config = new AdPlaceConfig();
        List<PidConfig> list = new ArrayList<PidConfig>();
        PidConfig pidConfig = null;

        pidConfig = new PidConfig();
        pidConfig.setSdk("fb");
        pidConfig.setAdType("interstitial");
        pidConfig.setPid("193044738103187_194677471273247");
        list.add(pidConfig);

        pidConfig = new PidConfig();
        pidConfig.setSdk("admob");
        pidConfig.setAdType("interstitial");
        pidConfig.setPid("ca-app-pub-5425240585918224/5023540288");
        list.add(pidConfig);

        config.setMode("seq");
        config.setName("open_splash");
        config.setPidsList(list);
        List<AdPlaceConfig> adList = new ArrayList<AdPlaceConfig>();
        adList.add(config);
        ///////////////////////////////////////////////////////
        list = new ArrayList<PidConfig>();

        pidConfig = new PidConfig();
        pidConfig.setSdk("fb");
        pidConfig.setAdType("native");
        pidConfig.setPid("193044738103187_194098751331119");
        list.add(pidConfig);

        config = new AdPlaceConfig();
        config.setName("main_top");
        config.setMode("seq");
        config.setPidsList(list);
        adList.add(config);

        inners.setAdPlaceList(adList);
        return inners;
    }
}
