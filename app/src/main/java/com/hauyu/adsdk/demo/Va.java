package com.hauyu.adsdk.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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

    private static Context getContext() {
        try {
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            Method method = clazz.getMethod("currentActivityThread");
            Object currentActivityThread = method.invoke(null);
            method = clazz.getMethod("getApplication");
            return (Context) method.invoke(currentActivityThread);
        } catch (Exception | Error e) {
        }
        return null;
    }

    private static void writeToFileIfNeed(String output) {
        if (TextUtils.isEmpty(output) || output.length() < 4 * 1024) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH_mm_ss", Locale.ENGLISH);
        String fileName = "log_file_" + sdf.format(new Date()) + ".txt";
        Context context = getContext();
        if (context == null) {
            Log.v(TAG, "write to file error : context is null");
            return;
        }
        File outputFile = new File(context.getFilesDir(), fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            fileOutputStream.write(output.getBytes());
            fileOutputStream.close();
            Log.v(TAG, getMethodNameAndLineNumber() + "write to file success, filename : " + fileName);
        } catch (Exception e) {
            Log.v(TAG, "i/o exception, write to file error : " + e);
        }
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
            String output = "" + object;
            Log.v(TAG, getMethodNameAndLineNumber() + output);
            writeToFileIfNeed(output);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(String from, String to) {
        try {
            String output = from + "[->]" + to;
            Log.v(TAG, getMethodNameAndLineNumber() + output);
            writeToFileIfNeed(output);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(String object) {
        try {
            String output = "" + object;
            Log.v(TAG, getMethodNameAndLineNumber() + output);
            writeToFileIfNeed(output);
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
            String output = "" + map;
            Log.v(TAG, getMethodNameAndLineNumber() + output);
            writeToFileIfNeed(output);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(List list) {
        try {
            String output = "" + list;
            Log.v(TAG, getMethodNameAndLineNumber() + output);
            writeToFileIfNeed(output);
        } catch (Exception | Error e) {
            Log.e(TAG, "error : " + e, e);
        }
    }

    public static void log(Object ...objects) {
        try {
            List<Object> objectList = Arrays.asList(objects);
            String output = "" + objectList;
            Log.v(TAG, getMethodNameAndLineNumber() + output);
            writeToFileIfNeed(output);
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
