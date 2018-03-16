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
    private OnDataListener mOnDataListener;

    public HttpDataRequest(Context context, String url) {
        mContext = context;
        mUrl = url;
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
                Log.d(Log.TAG, "content : " + content);
                if (mOnDataListener != null) {
                    mOnDataListener.onData(content);
                }
            }

            @Override
            public void onFailure(int code, String error) {
                Log.e(Log.TAG, "error : " + error + "(" + code + ")");
                if (mOnDataListener != null) {
                    mOnDataListener.onData(null);
                }
            }
        });
    }

    @Override
    public void setOnDataListener(OnDataListener l) {
        mOnDataListener = l;
    }
}
