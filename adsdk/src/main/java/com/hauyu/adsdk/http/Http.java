package com.hauyu.adsdk.http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.log.Log;

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
        private final String namePrefix = "hauyu-";
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

    private Handler mHandler;
    private Http() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    private static Http sHttp;

    public static Http get() {
        synchronized (Http.class) {
            if (sHttp == null) {
                createInstance();
            }
        }
        return sHttp;
    }

    private static void createInstance() {
        synchronized (Http.class) {
            if (sHttp == null) {
                sHttp = new Http();
            }
        }
    }

    private Http(Context context) {
    }

    public void request(String url, Map<String, String> header, OnCallback callback) {
        final Request r = new Request();
        r.setCallback(callback);
        r.setHeader(header);
        r.setUrl(addQuery(url, "random=" + System.currentTimeMillis()));
        Log.d(Log.TAG, "request() url : " + r.getUrl());
        sService.execute(new Runnable() {
            @Override
            public void run() {
                UrlHttp urlHttp = new UrlHttp();
                Response response = urlHttp.execute(r);
                if (response != null && response.getStatusCode() == 200) {
                    parseResponse(r, response);
                } else {
                    deliverFailure(r, response.getStatusCode(), response.getError());
                }
            }
        });
    }

    private void parseResponse(final Request request, Response response) {
        String content = new String(response.getContent());
        deliverSuccess(request, content);
    }

    private void deliverSuccess(final Request request, final String content) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (request != null && request.getCallback() != null) {
                    request.getCallback().onSuccess(content);
                }
            }
        });
    }

    private void deliverFailure(final Request request, final int code, final String error) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (request != null && request.getCallback() != null) {
                    request.getCallback().onFailure(code, error);
                }
            }
        });
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

    public void download(String url, String filePath, Map<String, String> header, OnCallback callback) {
        final DownloadRequest r = new DownloadRequest();
        r.setCallback(callback);
        r.setHeader(header);
        r.setUrl(addQuery(url, "random=" + System.currentTimeMillis()));
        r.setFilePath(filePath);
        Log.d(Log.TAG, "download() url : " + r.getUrl());
        sService.execute(new Runnable() {
            @Override
            public void run() {
                UrlHttp urlHttp = new UrlHttp();
                Response response = urlHttp.download(r);
                if (response != null && response.getStatusCode() == 200) {
                    parseResponse(r, response);
                } else {
                    deliverFailure(r, response.getStatusCode(), response.getError());
                }
            }
        });
    }
}