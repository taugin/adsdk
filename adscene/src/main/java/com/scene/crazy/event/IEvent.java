package com.scene.crazy.event;

import android.content.Context;

/**
 * Created by Administrator on 2018/2/9.
 */

public interface IEvent {

    /**
     * GT广告请求
     * @param context
     */
    public void reportAdSceneRequest(Context context, String adOuterType, String pidName);

    /**
     * GT广告加载成功
     * @param context
     */
    public void reportAdSceneLoaded(Context context, String adOuterType, String pidName);

    /**
     * GT广告展示
     * @param context
     */
    public void reportAdSceneShow(Context context, String adOuterType, String pidName);

    /**
     * GT广告展示成功
     * @param context
     */
    public void reportAdSceneImp(Context context, String adOuterType, String pidName);

    /**
     * GT不允许展示
     * @param context
     */
    public void reportAdSceneDisallow(Context context, String adOuterType, String pidName);

    /**
     * GT广告展示次数
     * @param context
     * @param times
     */
    public void reportAdSceneShowTimes(Context context, String adOuterType, int times);
}
