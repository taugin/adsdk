package com.inner.adsdk.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.inner.adsdk.log.Log;
import com.inner.adsdk.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2018/1/17.
 */

public class Http {
    private static ThreadFactory sFactory = new ThreadFactory() {
        private final String namePrefix = "inner-";
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r,
                    namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    };
    private static final ExecutorService sService = Executors.newFixedThreadPool(2, sFactory);
    private static final Map<String, Request> sDownloadMap = new HashMap<String, Request>();

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Context mContext;

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
    }

    public void request(String url, Map<String, String> header, OnCallback callback) {
        final Request r = new Request();
        r.setCallback(callback);
        r.setHeader(header);
        r.setUrl(url);
        requestHttpInternal(r, header);
    }

    private void requestHttpInternal(final Request request, Map<String, String> header) {
        if (request == null) {
            deliverFailure(request, -1, "unknown");
            return;
        }
        Log.iv(Log.TAG, "url : " + request.getUrl());
        final Request r = request;
        sService.execute(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                response = readDataFromCache(r);
                Log.iv(Log.TAG, "read data from cache : " + (response != null));
                if (response == null) {
                    try {
                        UrlHttp urlHttp = new UrlHttp();
                        response = urlHttp.execute(r);
                        response.setCache(false);
                    } catch (Exception e) {
                        Log.e(Log.TAG, "error : " + e);
                    }
                }
                if (response != null && response.getStatusCode() == 200) {
                    parseResponse(r, response);
                } else if (response != null){
                    deliverFailure(r, response.getStatusCode(), response.getError());
                } else {
                    deliverFailure(r, -1, "unknown");
                }
            }
        });
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

    private void writeDataToCache(Request request, Response response) {
        Log.iv(Log.TAG, "write data to cache");
        try {
            byte buf[] = response.getContent();
            String filePath = getCacheFilePath(request.getUrl());
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(buf);
            fos.close();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    private Response readDataFromCache(Request request) {
        Log.iv(Log.TAG, "read data from cache");
        try {
            String filePath = getCacheFilePath(request.getUrl());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(filePath);
            byte buf [] = new byte[1024];
            int len = 0;
            while ((len = fis.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            fis.close();
            buf = baos.toByteArray();
            baos.close();
            Response response = new Response();
            response.setStatusCode(200);
            response.setContent(buf);
            response.setCache(true);
            return response;
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private void parseResponse(final Request request, Response response) {
        Log.iv(Log.TAG, "parse response : " + request.getUrl());
        final OnCallback callback = request.getCallback();
        if (callback instanceof OnImageCallback) {
            try {
                byte[] content = response.getContent();
                Bitmap bitmap = BitmapFactory.decodeByteArray(content, 0, content.length);
                deliverBitmapSuccess(bitmap, (OnImageCallback) callback);
            } catch (Exception e) {
                Log.e(Log.TAG, "error : " + e);
                deliverFailure(request, response.getStatusCode(), response.getError());
            }
        } else if (callback instanceof OnStringCallback) {
            String content = new String(response.getContent());
            deliverStringSuccess(content, (OnStringCallback) callback);
        }
        if (request.isCache() && !response.isCache()) {
            writeDataToCache(request, response);
        }
    }

    private void deliverStringSuccess(final String content, final OnStringCallback callback) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onSuccess(content);
                    }
                }
            });
        }
    }

    private void deliverBitmapSuccess(final Bitmap bitmap, final OnImageCallback callback) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onSuccess(bitmap);
                    }
                }
            });
        }
    }

    private void deliverFailure(final Request request, final int code, final String error) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (request != null && request.getCallback() != null) {
                        request.getCallback().onFailure(code, error);
                    }
                }
            });
        }
    }

    private String addQuery(String url, String query) {
        try {
            if (url.indexOf("?") > 0) {
                if (url.endsWith("&")) {
                    url = url + query;
                } else {
                    url = url + "&" + query;
                }
            } else {
                url = url + "?" + query;
            }
        } catch (Exception e) {
        }
        return url;
    }

    public void loadImage(String url, Map<String, String> header, OnCallback callback) {
        final Request r = new Request();
        r.setCallback(callback);
        r.setHeader(header);
        r.setUrl(url);
        r.setCache(true);
        requestHttpInternal(r, header);
    }
}