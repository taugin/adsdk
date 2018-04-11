package com.inner.adaggs.request;

import android.content.Context;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;
import com.inner.adaggs.BuildConfig;
import com.inner.adaggs.log.Log;
import com.inner.adaggs.utils.Utils;

/**
 * Created by Administrator on 2018/2/12.
 */

public class GTagDataRequest implements IDataRequest {

    private static final boolean DEBUG = true;
    private static final boolean USE_LOCAL_CONFIG = BuildConfig.DEBUG && DEBUG;

    private static final int DEFAULT_CONTAINER = 0;
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
        if (mContainerHolder != null && mContainerHolder.getContainer() != null) {
            try {
                mContainerHolder.refresh();
                Log.v(Log.TAG, "container holder refresh");
            } catch(Exception e) {
            }
        }
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
    public String getString(String key) {
        if (USE_LOCAL_CONFIG) {
            Log.d(Log.TAG, "use local config");
            return readConfigFromAsset(key);
        } else {
            if (mContainerHolder != null) {
                Container container = mContainerHolder.getContainer();
                if (container != null) {
                    return container.getString(key);
                }
            }
        }
        return null;
    }

    private String readConfigFromAsset(String key) {
        return Utils.readAssets(mContext, key);
    }
}
