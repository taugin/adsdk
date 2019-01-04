package com.inner.adsdk.listener;

/**
 * Created by Administrator on 2019-1-4.
 */

public interface OnAdRewardListener {
    public void onRefresh(boolean adLoaded);

    public void onReward();

    public void onDismiss();

    public void onClick();

    public void onNoReward();

    public void onShow();

    public void onLoaded();
}
