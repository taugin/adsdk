package com.hauyu.adsdk.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/14.
 */

public class Va extends ProxySelector implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "va";
    private static final String PROXY_HOST = "172.16.170.247";
    private static final String PROXY_PORT = "8888";
    private static final List<Proxy> sProxyList;

    static {
        sProxyList = new ArrayList<>();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, Integer.parseInt(PROXY_PORT)));
        sProxyList.add(proxy);
    }

    public static void setNetworkProxy() {
        System.setProperty("http.proxyHost", PROXY_HOST);
        System.setProperty("http.proxyPort", PROXY_PORT);
        System.setProperty("https.proxyHost", PROXY_HOST);
        System.setProperty("https.proxyPort", PROXY_PORT);
    }

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(new Va());
    }

    public static void setAutoProxy() {
        ProxySelector.setDefault(new Va());
    }

    @Override
    public List<Proxy> select(URI uri) {
        log("ProxySelector.select uri : " + uri);
        return sProxyList;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        log("ProxySelector.connectFailed uri : " + uri + " , sa : " + sa + " , ioe : " + ioe);
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
            String output = "" + object + "\n";
            output += "++++++++++++++++++++++++++++++++\n";
            Field fields[] = object.getClass().getDeclaredFields();
            String className = object.getClass().getSimpleName();
            if (fields != null && fields.length > 0) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object fieldValue = field.get(object);
                    output = output + "[" + className + "]" + fieldName + " : " + fieldValue + "\n";
                    field.setAccessible(false);
                }
            }
            output += "================================\n";
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

    public static void log(Object... objects) {
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
                    methodName, lineNumber, Locale.ENGLISH);
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

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        log("onActivityCreated activity : " + activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        log("onActivityStarted activity : " + activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        log("onActivityResumed activity : " + activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        log("onActivityPaused activity : " + activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        log("onActivityStopped activity : " + activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        log("onActivitySaveInstanceState activity : " + activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        log("onActivityDestroyed activity : " + activity);
    }
}
