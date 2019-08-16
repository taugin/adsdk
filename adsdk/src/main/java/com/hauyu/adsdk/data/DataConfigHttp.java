package com.hauyu.adsdk.data;

import android.content.Context;
import android.text.TextUtils;

import com.hauyu.adsdk.core.BaseRequest;
import com.hauyu.adsdk.http.Http;
import com.hauyu.adsdk.http.OnStringCallback;
import com.hauyu.adsdk.log.Log;


/**
 * Created by Administrator on 2018/2/12.
 */

public class DataConfigHttp extends BaseRequest {

    private Context mContext;
    private String mUrl;
    private String mContent;

    public DataConfigHttp(Context context) {
        mContext = context;
    }

    @Override
    public void setUrl(String address) {
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
}
