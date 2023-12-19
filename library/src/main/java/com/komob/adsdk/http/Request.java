package com.komob.adsdk.http;

import java.util.Map;

/**
 * Created by Administrator on 2018/1/17.
 */

class Request {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private String url;
    private Map<String, String> header;
    private int connectTimeout;
    private int readTimeout;
    private OnCallback callback;
    private long startTime;
    private boolean cache;
    private byte[] data;
    private String method = GET;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public OnCallback getCallback() {
        return callback;
    }

    public void setCallback(OnCallback callback) {
        this.callback = callback;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "Request{" +
                "url='" + url + '\'' +
                ", header=" + header +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", callback=" + callback +
                ", startTime=" + startTime +
                '}';
    }
}