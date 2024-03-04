package com.komob.adsdk.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.komob.adsdk.http.volley.AuthFailureError;
import com.komob.adsdk.http.volley.Request;
import com.komob.adsdk.http.volley.RequestQueue;
import com.komob.adsdk.http.volley.Response;
import com.komob.adsdk.http.volley.VolleyError;
import com.komob.adsdk.http.volley.toolbox.ImageRequest;
import com.komob.adsdk.http.volley.toolbox.StringRequest;
import com.komob.adsdk.http.volley.toolbox.Volley;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/17.
 */

public class Http {
    private Context mContext;

    private RequestQueue mRequestQueue;

    private static Http sHttp;

    public static Http get(Context context) {
        synchronized (Http.class) {
            if (sHttp == null) {
                createInstance(context);
            }
        }
        return sHttp;
    }

    private static void createInstance(Context context) {
        synchronized (Http.class) {
            if (sHttp == null) {
                sHttp = new Http(context);
            }
        }
    }

    private Http(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public void get(String url, Map<String, String> headers, OnStringCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (callback != null) {
                    String errorMessage = null;
                    if (error != null) {
                        errorMessage = error.getMessage();
                    }
                    int statusCode = -1;
                    try {
                        statusCode = error.networkResponse.statusCode;
                    } catch (Exception e) {
                    }
                    callback.onFailure(statusCode, errorMessage);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        if (mRequestQueue != null) {
            mRequestQueue.add(stringRequest);
        }
    }

    public void post(String url, Map<String, String> headers, byte[] data, OnStringCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (callback != null) {
                    callback.onSuccess(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (callback != null) {
                    String errorMessage = null;
                    if (error != null) {
                        errorMessage = error.getMessage();
                    }
                    int statusCode = -1;
                    try {
                        statusCode = error.networkResponse.statusCode;
                    } catch (Exception e) {
                    }
                    callback.onFailure(statusCode, errorMessage);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return data;
            }
        };
        if (mRequestQueue != null) {
            mRequestQueue.add(stringRequest);
        }
    }

    private String getCacheFilePath(String url) {
        try {
            String fileName = Utils.string2MD5(url);
            File cacheFile = mContext.getCacheDir();
            File adCache = new File(cacheFile, "spread");
            adCache.mkdirs();
            File bannerFile = new File(adCache, fileName);
            return bannerFile.getAbsolutePath();
        } catch (Exception e) {
        }
        return null;
    }

    private void writeDataToCache(String url, Bitmap bitmap) {
        try {
            String filePath = getCacheFilePath(url);
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);
            fos.close();
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
    }

    private Bitmap readDataFromCache(String url) {
        try {
            String filePath = getCacheFilePath(url);
            if (new File(filePath).exists()) {
                return BitmapFactory.decodeFile(filePath);
            }
        } catch (Exception e) {
            Log.iv(Log.TAG, "error : " + e);
        }
        return null;
    }

    public void loadImage(String url, ImageView.ScaleType scaleType, OnImageCallback onImageCallback) {
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                if (onImageCallback != null) {
                    onImageCallback.onSuccess(bitmap);
                }
            }
        }, 0, 0, scaleType, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (onImageCallback != null) {
                    String errorMessage = null;
                    if (error != null) {
                        errorMessage = error.getMessage();
                    }
                    int statusCode = -1;
                    try {
                        statusCode = error.networkResponse.statusCode;
                    } catch (Exception e) {
                    }
                    onImageCallback.onFailure(statusCode, errorMessage);
                }
            }
        });
        if (mRequestQueue != null) {
            mRequestQueue.add(imageRequest);
        }
    }
}