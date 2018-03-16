package com.inner.adaggs.parse;

import com.inner.adaggs.config.AdConfig;
import com.inner.adaggs.config.DevInfo;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IParser {

    String WHITE_LIST = "wdevs";
    String IMEI = "imei";
    String AID = "aid";
    String STATUS = "s";
    String DATA = "data";

    String ADIDS = "adids";
    String POLICY = "policy";
    String ADPLACES = "adplaces";
    String NAME = "name";
    String MODE = "mode";
    String PTYPE = "ptype";

    String MAXCOUNT = "maxcount";
    String PERCENT = "percent";
    String PIDS = "pids";

    String SDK = "sdk";
    String PID = "pid";
    String CTR = "ctr";
    String TYPE = "type";

    String OUTER = "outer";
    String FILM = "film";

    int parseStatus(String data);
    List<DevInfo> parseDevList(String data);
    String parseContent(String data);
    AdConfig parse(String data);

}
