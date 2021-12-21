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

#mintegral
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mintegral.** {*; }
-keep interface com.mintegral.** {*; }
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-keep interface androidx.** { *; }
-keep class androidx.** { *; }
-keep public class * extends androidx.** { *; }
-dontwarn com.mintegral.**
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mintegral*; }
-keep class **.R$* { public static final int mbridge*; }
-keep class com.alphab.** {*; }
-keep interface com.alphab.** {*; }

#tradplus
-keep public class com.tradplus.** { *; }
-keep class com.tradplus.ads.** { *; }

#topon
-keep public class com.anythink.**
-keepclassmembers class com.anythink.** {
   *;
}

-keep public class com.anythink.network.**
-keepclassmembers class com.anythink.network.** {
   public *;
}

-dontwarn com.anythink.hb.**
-keep class com.anythink.hb.**{ *;}

-dontwarn com.anythink.china.api.**
-keep class com.anythink.china.api.**{ *;}

# new in v5.6.6
-keep class com.anythink.myoffer.ui.**{ *;}
-keepclassmembers public class com.anythink.myoffer.ui.** {
   public *;
}
