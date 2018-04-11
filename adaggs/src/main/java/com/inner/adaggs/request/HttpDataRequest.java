package com.inner.adaggs.request;

import android.content.Context;
import android.text.TextUtils;

import com.inner.adaggs.http.Http;
import com.inner.adaggs.http.OnCallback;
import com.inner.adaggs.log.Log;


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
        Log.d(Log.TAG, "request config url : " + url);
        Http.get().request(url, null, new OnCallback() {
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
    public String getString(String key) {
        return null;
    }
}
