package com.inner.adsdk.request;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;
import com.inner.adsdk.constant.Constant;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import java.util.Date;

/**
 * Created by Administrator on 2018/2/12.
 */

public class GTagDataRequest implements IDataRequest {

    private static final int DEFAULT_CONTAINER = 0;
    private static final int REFRESH_INTERVAL = 15 * 60 * 1000;
    private Context mContext;
    private String mContainerId;
    private ContainerHolder mContainerHolder;

    public GTagDataRequest(Context context) {
        mContext = context;
    }

    @Override
    public void setAddress(String address) {
        mContainerId = address;
    }

    @Override
    public void request() {
        TagManager tagManager = TagManager.getInstance(mContext);
        PendingResult<ContainerHolder> pending =
                tagManager.loadContainerPreferNonDefault(mContainerId, DEFAULT_CONTAINER);
        pending.setResultCallback(new ResultCallback<ContainerHolder>() {
            @Override
            public void onResult(ContainerHolder containerHolder) {
                mContainerHolder = containerHolder;
                if (!containerHolder.getStatus().isSuccess()) {
                    Log.e(Log.TAG, "failure loading container");
                }
            }
        });
    }

    @Override
    public void refresh() {
        if (mContainerHolder != null && mContainerHolder.getContainer() != null) {
            long lastRefreshTime = mContainerHolder.getContainer().getLastRefreshTime();
            long now = System.currentTimeMillis();
            Log.v(Log.TAG, "GTagDataRequest refresh now : " + Constant.SDF_1.format(new Date(now)) + " , last : " + Constant.SDF_1.format(new Date(lastRefreshTime)));
            if (now - lastRefreshTime > REFRESH_INTERVAL) {
                try {
                    mContainerHolder.refresh();
                    Log.v(Log.TAG, "container holder refresh");
                } catch (Exception e) {
                }
            }
        } else {
            request();
        }
    }

    @Override
    public String getString(String key) {
        String value = readConfigFromAsset(key);
        Log.v(Log.TAG, "local config : " + key + " , value : " + value);
        if (TextUtils.isEmpty(value)) {
            if (mContainerHolder != null) {
                Container container = mContainerHolder.getContainer();
                if (container != null) {
                    value = container.getString(key);
                }
            }
        }
        return value;
    }

    private String readConfigFromAsset(String key) {
        return Utils.readAssets(mContext, key);
    }
}
