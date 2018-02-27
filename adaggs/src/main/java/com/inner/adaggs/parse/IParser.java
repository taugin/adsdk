package com.inner.adaggs.parse;

import com.inner.adaggs.config.AdConfig;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IParser {
    public AdConfig parse(String content);
}
