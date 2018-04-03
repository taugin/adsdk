package com.inner.adaggs;

import com.inner.adaggs.constant.Constant;

/**
 * Created by Administrator on 2018/2/11.
 */

public class AdExtra {
    /**
     * 尺寸 320 x 50
     */
    public static final int ADMOB_BANNER = Constant.BANNER;

    /**
     * 尺寸 468 x 60
     */
    public static final int ADMOB_FULL_BANNER = Constant.FULL_BANNER;

    /**
     * 尺寸 320 x 100
     */
    public static final int ADMOB_LARGE_BANNER = Constant.LARGE_BANNER;

    /**
     * 尺寸 728 x 90
     */
    public static final int ADMOB_LEADERBOARD = Constant.LEADERBOARD;

    /**
     * 尺寸 300 x 250
     */
    public static final int ADMOB_MEDIUM_RECTANGLE = Constant.MEDIUM_RECTANGLE;

    /**
     * 尺寸 160 x 600
     */
    public static final int ADMOB_WIDE_SKYSCRAPER = Constant.WIDE_SKYSCRAPER;

    /**
     * 尺寸 smart_banner
     */
    public static final int ADMOB_SMART_BANNER = Constant.SMART_BANNER;

    /**
     * 尺寸 w x 50
     */
    public static final int FB_BANNER = Constant.BANNER;

    /**
     * 尺寸 w x 90
     */
    public static final int FB_LARGE_BANNER = Constant.LARGE_BANNER;

    /**
     * 尺寸 w x 250
     */
    public static final int FB_MEDIUM_RECTANGLE = Constant.MEDIUM_RECTANGLE;

    /**
     * banner 尺寸后缀
     */
    public static final String BANNER_SIZE_SUFFIX = "_banner_size";

    /**
     * 原生视图后缀
     */
    public static final String ROOT_VIEW_SUFFIX = "_rootview";

    /**
     * 原生视图模板后缀
     */
    public static final String TEMPLATE_SUFFIX = "_template";

    /**
     * 定义Admob的Banner大小
     */
    public static final String KEY_ADMOB_BANNER_SIZE = Constant.AD_SDK_ADMOB + BANNER_SIZE_SUFFIX;

    /**
     * 定义Adx的Banner大小
     */
    public static final String KEY_ADX_BANNER_SIZE = Constant.AD_SDK_ADX + BANNER_SIZE_SUFFIX;

    /**
     * 定义Facebook的Banner大小
     */
    public static final String KEY_FB_BANNER_SIZE = Constant.AD_SDK_FACEBOOK + BANNER_SIZE_SUFFIX;

    /**
     * 传递facebook的rootview
     */
    public static final String KEY_FB_NATIVE_ROOTVIEW = Constant.AD_SDK_FACEBOOK + ROOT_VIEW_SUFFIX;

    /**
     * 传递facebook的原生广告模板
     */
    public static final String KEY_FB_NATIVE_TEMPLATE = Constant.AD_SDK_FACEBOOK + TEMPLATE_SUFFIX;

    /**
     * facebook原生广告模板值
     */
    public static final int FB_NATIVE_TEMPLATE_SMALL = Constant.FB_NATIVE_SMALL;

    /**
     * facebook原生广告模板值
     */
    public static final int FB_NATIVE_TEMPLATE_MEDIUM = Constant.FB_NATIVE_MEDIUM;

    /**
     * facebook原生广告模板值
     */
    public static final int FB_NATIVE_TEMPLATE_LARGE = Constant.FB_NATIVE_LARGE;
}
