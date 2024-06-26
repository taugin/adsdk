package com.inner.adsdk.http;

import android.text.TextUtils;

import com.inner.adsdk.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Administrator on 2018/1/17.
 */

class UrlHttp {

    private Request mRequest;


    public Response execute(Request request) {
        mRequest = request;
        if (mRequest == null) {
            return null;
        }
        HttpURLConnection conn = createConnection(mRequest.getUrl());
        configConnection(conn);
        addHeader(conn, mRequest.getHeader());
        return request(conn);
    }

    private Response request(HttpURLConnection conn) {
        Response response = new Response();
        response.setStatusCode(HttpURLConnection.HTTP_NOT_FOUND);
        try {
            conn.connect();
            int respCode = conn.getResponseCode();
            response.setStatusCode(respCode);
            if (respCode == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int read = 0;
                while((read = is.read(buf)) > 0) {
                    baos.write(buf, 0, read);
                }
                is.close();
                response.setContent(baos.toByteArray());
                baos.close();
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
            if (e instanceof SocketTimeoutException) {
                response.setStatusCode(HttpURLConnection.HTTP_CLIENT_TIMEOUT);
                response.setError(e.getMessage());
            } else if (e instanceof ConnectException) {
                response.setStatusCode(HttpURLConnection.HTTP_SERVER_ERROR);
                response.setError(e.getMessage());
            } else {
                response.setError(e.getMessage());
            }
        }
        return response;
    }

    private HttpURLConnection createConnection(String uri) {
        try {
            URL url = new URL(uri);
            URLConnection conn = url.openConnection();
            return (HttpURLConnection) conn;
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
        return null;
    }

    private void configConnection(HttpURLConnection conn) {
        // 配置https信任
        if (conn instanceof HttpsURLConnection) {
            HttpsConfig.configHttps((HttpsURLConnection)conn);
        }
        conn.setConnectTimeout(mRequest.getConnectTimeout());
        conn.setReadTimeout(mRequest.getReadTimeout());
    }

    private void addHeader(HttpURLConnection conn, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!TextUtils.isEmpty(key)) {
                    conn.addRequestProperty(key, value);
                }
            }
        }
    }
}
