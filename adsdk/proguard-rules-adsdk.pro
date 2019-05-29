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
-keep class com.umeng.commonsdk.** {*;}
-keep class com.umeng.analytics.** {*;}

#appsflyer Analytics
-keep class com.appsflyer.** {*;}

#Firebase Analytics
-keep class com.google.firebase.analytics.FirebaseAnalytics{*;}

#Facebook Analytics
-keep class com.facebook.appevents.AppEventsLogger {*;}

#MoPub
# Keep public classes and methods.
-keepclassmembers class com.mopub.** { public *; }
-keep public class com.mopub.**
-keep public class android.webkit.JavascriptInterface {}

-keepclassmembers class com.mopub.volley.** { *; }
-keep public class com.mopub.volley.**
-dontwarn com.mopub.volley.**

-dontwarn com.mopub.mobileads.**
-dontwarn com.mopub.nativeads.**

# Explicitly keep any custom event classes in any package.
-keep class * extends com.mopub.mobileads.CustomEventBanner {}
-keep class * extends com.mopub.mobileads.CustomEventInterstitial {}
-keep class * extends com.mopub.nativeads.CustomEventNative {}
-keep class * extends com.mopub.nativeads.CustomEventRewardedAd {}

# Keep methods that are accessed via reflection
-keepclassmembers class ** { @com.mopub.common.util.ReflectionTarget *; }

# Viewability support
-keepclassmembers class com.integralads.avid.library.mopub.** { public *; }
-keep public class com.integralads.avid.library.mopub.**
-keepclassmembers class com.moat.analytics.mobile.mpub.** { public *; }
-keep public class com.moat.analytics.mobile.mpub.**

# Support for Android Advertiser ID.
-keep class com.google.android.gms.common.GooglePlayServicesUtil {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {*;}

# Support for Google Play Services
# http://developer.android.com/google/play-services/setup.html
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

#inmobi
-keepattributes SourceFile,LineNumberTable
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
     public *;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{
     public *;
}
# skip the Picasso library classes
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**
# skip Moat classes
-keep class com.moat.** {*;}
-dontwarn com.moat.**
# skip AVID classes
-keep class com.integralads.avid.library.* {*;}

# Gson
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod
-keep class com.fyber.** { *; }

# DAP
-keep class com.duapps.ad.**{*;}
-dontwarn com.duapps.ad.**
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
 @com.google.android.gms.common.annotation.KeepName *;}
-keep class com.google.android.gms.common.GooglePlayServicesUtil {
 public <methods>;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{
 public <methods>;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
 public <methods>;
}

# Altamob
-dontwarn java.lang.invoke.*
-keep class com.altamob.** {*;}
-keep class com.mobi.** {*;}
-keepclassmembers class * {
  @android.webkit.JavascriptInterface <methods>;
}

# CloudMobi
#for sdk
-keep public class com.cloudtech.**{*;}
-dontwarn com.cloudtech.**
#for gaid
-keep class **.AdvertisingIdClient$** { *; }
#for js and webview interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
#Facebook
-dontwarn com.adywind.nativeads.**

#display io
-keep class io.display.sdk.Controller.** { *;}
-dontwarn io.display.sdk.Controller.**