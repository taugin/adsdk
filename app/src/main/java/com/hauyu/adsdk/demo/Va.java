package com.hauyu.adsdk.demo;

import android.annotation.SuppressLint;
import android.util.Log;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/14.
 */

public class Va {

    private static final String TAG = "va";
    private static final String PROXY_HOST = "172.16.170.218";
    private static final String PROXY_PORT = "8888";

    public static void setNetworkProxy() {
        System.setProperty("http.proxyHost", PROXY_HOST);
        System.setProperty("http.proxyPort", PROXY_PORT);
        System.setProperty("https.proxyHost", PROXY_HOST);
        System.setProperty("https.proxyPort", PROXY_PORT);
    }

    public static void dumpStack() {
        try {
            Log.e(TAG, "", new Throwable());
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(Object object) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + object);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(String object) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + object);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(float object) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + object);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(double object) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + object);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(short object) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + object);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(int object) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + object);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(byte object) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + object);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(long object) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + object);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(boolean object) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + object);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(Map map) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + map);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(List list) {
        try {
            Log.v(TAG, getMethodNameAndLineNumber() + list);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log() {
        try {
            Log.v(TAG, getMethodNameAndLineNumber());
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    @SuppressLint("DefaultLocale")
    private static String getMethodNameAndLineNumber() {
        StackTraceElement element[] = Thread.currentThread().getStackTrace();
        if (element != null && element.length >= 4) {
            String methodName = element[4].getMethodName();
            int lineNumber = element[4].getLineNumber();
            return String.format("%s.%s : %d ---> ", getClassName(),
                    methodName, lineNumber, Locale.CHINESE);
        }
        return null;
    }

    private static String getClassName() {
        StackTraceElement element[] = Thread.currentThread().getStackTrace();
        if (element != null && element.length >= 4) {
            String className = element[5].getClassName();
            if (className == null) {
                return null;
            }
            int index = className.lastIndexOf(".");
            if (index != -1) {
                className = className.substring(index + 1);
            }
            index = className.indexOf('$');
            if (index != -1) {
                className = className.substring(0, index);
            }
            return className;
        }
        return null;
    }
}
