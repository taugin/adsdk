# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-keepattributes SourceFile,LineNumberTable

#所有view子类不混淆
-keep class * extends android.view.View {public *; protected *;}
#activity子类public和protected不混淆
-keep class * extends android.app.Activity {public *; protected *;}

#umeng
-keep class com.umeng.** {*;}
-keep class com.uc.** {*;}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#appsflyer Analytics
-keep class com.appsflyer.** {*;}

#Firebase Analytics
-keep class com.google.firebase.analytics.FirebaseAnalytics{*;}

#Facebook Analytics
-keep class com.facebook.appevents.AppEventsLogger {*;}
-dontwarn com.adywind.nativeads.**
-dontwarn com.mopub.**

#Firebase Analytics
-keep class com.google.firebase.remoteconfig.FirebaseRemoteConfig{*;}
-keep class com.flurry.** {*;}

# 保留applovin下的所有类及其内部类
-keep class com.applovin.** {*;}
-dontwarn com.applovin.**