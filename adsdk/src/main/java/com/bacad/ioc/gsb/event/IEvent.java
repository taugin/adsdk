package com.bacad.ioc.gsb.event;

import android.content.Context;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IEvent {

    /**
     * GT广告请求
     * @param context
     */
    public void reportAdOuterRequest(Context context, String adOuterType, String pidName);

    /**
     * GT广告加载成功
     * @param context
     */
    public void reportAdOuterLoaded(Context context, String adOuterType, String pidName);

    /**
     * GT广告展示
     * @param context
     */
    public void reportAdOuterCallShow(Context context, String adOuterType, String pidName);

    /**
     * GT广告展示成功
     * @param context
     */
    public void reportAdOuterShowing(Context context, String adOuterType, String pidName);

    /**
     * GT不允许展示
     * @param context
     */
    public void reportAdOuterDisallow(Context context, String adOuterType, String pidName);

    /**
     * GT广告展示次数
     * @param context
     * @param times
     */
    public void reportAdOuterShowTimes(Context context, String adOuterType, int times);
}
