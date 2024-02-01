package com.komob.adsdk.log;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;
import java.util.Locale;

public class Log {

    private static final boolean GLOBAL_TAG = true;
    private static final int VERBOSE = android.util.Log.VERBOSE;
    private static final int DEBUG = android.util.Log.DEBUG;
    private static final int INFO = android.util.Log.INFO;
    private static final int ERROR = android.util.Log.ERROR;
    private static final int WARN = android.util.Log.WARN;
    private static final boolean INTERNAL_LOG_ENABLE;

    public static final String TAG = "komob";
    public static final String TAG_SDK = "komob2";

    static {
        boolean internal = false;
        try {
            File tagFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File debugFile = new File(tagFolder, ".debug");
            internal = debugFile.exists();
        } catch (Exception e) {
        }
        INTERNAL_LOG_ENABLE = internal;
    }

    private static boolean isLoggable(String tag, int level) {
        return android.util.Log.isLoggable(tag, level);
    }

    public static void d(String tag, String message) {
        tag = checkLogTag(tag);
        if (isLoggable(tag, DEBUG)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.d(tag, extraString + message);
        }
    }

    public static void v(String tag, String message) {
        tag = checkLogTag(tag);
        if (isLoggable(tag, VERBOSE)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.v(tag, extraString + message);
        }
    }

    public static void iv(String tag, String message) {
        tag = checkLogTag(tag);
        if (isLoggable(tag, VERBOSE) && INTERNAL_LOG_ENABLE) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.v(tag, extraString + message);
        }
    }

    public static void i(String tag, String message) {
        tag = checkLogTag(tag);
        if (isLoggable(tag, INFO)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.i(tag, extraString + message);
        }
    }

    public static void w(String tag, String message) {
        tag = checkLogTag(tag);
        if (isLoggable(tag, WARN)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.w(tag, extraString + message);
        }
    }

    public static void e(String tag, String message) {
        tag = checkLogTag(tag);
        if (isLoggable(tag, ERROR)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.e(tag, extraString + message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        tag = checkLogTag(tag);
        if (isLoggable(tag, ERROR)) {
            String extraString = getMethodNameAndLineNumber();
            tag = privateTag() ? tag : getTag();
            android.util.Log.e(tag, extraString + message, throwable);
        }
    }

    private static boolean privateTag() {
        return GLOBAL_TAG;
    }

    private static String checkLogTag(String tag) {
        if (tag != null && tag.length() > 23) {
            tag = TAG;
        }
        return tag;
    }

    @SuppressLint("DefaultLocale")
    private static String getMethodNameAndLineNumber() {
        try {
            StackTraceElement element[] = Thread.currentThread().getStackTrace();
            if (element != null && element.length > 4) {
                String methodName = element[4].getMethodName();
                int lineNumber = element[4].getLineNumber();
                return String.format("%s.%s : %d ---> ", getClassName(),
                        methodName, lineNumber, Locale.ENGLISH);
            }
        } catch (Exception e) {
        }
        return "";
    }

    private static String getTag() {
        try {
            StackTraceElement element[] = Thread.currentThread().getStackTrace();
            if (element != null && element.length > 4) {
                String className = element[4].getClassName();
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
        } catch (Exception e) {
        }
        return TAG;
    }

    private static String getClassName() {
        StackTraceElement element[] = Thread.currentThread().getStackTrace();
        if (element != null && element.length > 5) {
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
        return "";
    }
}