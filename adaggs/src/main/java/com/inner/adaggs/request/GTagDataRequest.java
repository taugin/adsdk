package com.inner.adaggs.request;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;
import com.inner.adaggs.constant.Constant;
import com.inner.adaggs.log.Log;

/**
 * Created by Administrator on 2018/2/12.
 */

public class GTagDataRequest implements IDataRequest {

    private static final int DEFAULT_CONTAINER = 0;
    private Context mContext;
    private String mContainerId;
    private OnDataListener mOnDataListener;
    private Container mContainer;

    public GTagDataRequest(Context context, String containerId) {
        mContext = context;
        mContainerId = containerId;
    }

    @Override
    public void request() {
        TagManager tagManager = TagManager.getInstance(mContext);
        PendingResult<ContainerHolder> pending =
                tagManager.loadContainerPreferNonDefault(mContainerId, DEFAULT_CONTAINER);
        pending.setResultCallback(new ResultCallback<ContainerHolder>() {
            @Override
            public void onResult(ContainerHolder containerHolder) {
                Container container = containerHolder.getContainer();
                if (!containerHolder.getStatus().isSuccess()) {
                    Log.e(Log.TAG, "failure loading container");
                    return;
                }
                String data = null;
                if (container != null) {
                    data = container.getString(Constant.GTAG_ADS_CONFIG);
                }
                if (TextUtils.isEmpty(data)) {
                    Log.e(Log.TAG, "empty gtag ads config");
                    mContainer = container;
                } else {
                    Log.d(Log.TAG, "data : " + data);
                }
                if (mOnDataListener != null) {
                    mOnDataListener.onData(data);
                }
            }
        });
    }

    @Override
    public void setOnDataListener(OnDataListener l) {
        mOnDataListener = l;
    }

    @Override
    public String getString(String key) {
        if (mContainer != null) {
            return mContainer.getString(key);
        }
        return null;
    }
}
