package com.inner.adsdk.request;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adsdk.http.Http;
import com.inner.adsdk.http.OnStringCallback;
import com.inner.adsdk.log.Log;


/**
 * Created by Administrator on 2018/2/12.
 */

public class HttpDataRequest implements IDataRequest {

    private Context mContext;
    private String mUrl;
    private String mContent;

    public HttpDataRequest(Context context) {
        mContext = context;
    }

    @Override
    public void setAddress(String address) {
        mUrl = address;
    }

    @Override
    public void request() {
        String url = mUrl;
        Log.d(Log.TAG, "url : " + url);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Log.iv(Log.TAG, "request config url : " + url);
        Http.get(mContext).request(url, null, new OnStringCallback() {
            @Override
            public void onSuccess(String content) {
                mContent = content;
            }

            @Override
            public void onFailure(int code, String error) {
                Log.e(Log.TAG, "error : " + error + "(" + code + ")");
                mContent = null;
            }
        });
    }

    @Override
    public void refresh() {
    }

    @Override
    public String getString(String key) {
        return null;
    }
}
