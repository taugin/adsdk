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
-keepattributes SourceFile,LineNumberTable
-keep class com.inner.adaggs.AdAggs {*;}
-keep class com.inner.adaggs.AdExtra {*;}
-keep class com.inner.adaggs.AdParams {public *;}
-keep class com.inner.adaggs.AdParams$Builder {public *;}
-keep class com.inner.adaggs.listener.**{*;}

#如接入 Facebook 广告，须将下类可以添加到 proguard 配置：
-keep class com.facebook.ads.NativeAd
#如接入 Admob 广告，须将下类可以添加到 proguard 配置：
-keep class com.google.android.gms.ads.formats.NativeContentAd

#友盟代码混淆
-keep class com.umeng.commonsdk.** {*;}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

#appsflyer
-keep class com.appsflyer.** {*;}
-keep class com.google.firebase.**{*;}
-dontwarn com.appsflyer.**
-dontwarn com.google.firebase.**
-dontwarn com.android.installreferrer

#ads
-dontwarn com.mopub.**
-dontwarn com.google.**
-dontwarn com.umeng.**