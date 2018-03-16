package com.inner.adaggs.http;

import java.util.Map;

/**
 * Created by Administrator on 2018/1/17.
 */

class Response {
    private int statusCode;
    private byte[] content;
    private Map<String, String> header;
    private String error;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
