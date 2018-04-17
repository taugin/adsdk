package com.inner.adsdk.http;

/**
 * Created by Administrator on 2018/2/11.
 */

public class DownloadRequest extends Request {
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
