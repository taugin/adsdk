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
#-keep class * extends android.view.View {public *; protected *;}
#activity子类public和protected不混淆
#-keep class * extends android.app.Activity {public *; protected *;}

#################################################################
-renamesourcefileattribute SourceFile
-repackageclasses
#################################################################

-keep public class com.android.installreferrer.** { *; }
-keepclasseswithmembernames public class * extends android.app.Activity {public *;protected *;}

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

#Facebook Analytics
-keep class com.facebook.appevents.AppEventsLogger {*;}

#Facebook audience network
-keep class com.facebook.ads.** {*;}
-dontwarn com.facebook.ads.**

#Mopub
-dontwarn com.adywind.nativeads.**
-dontwarn com.mopub.**

#Firebase Analytics
-keep class com.google.firebase.analytics.FirebaseAnalytics{*;}

#Firebase Remote Config
-keep class com.google.firebase.remoteconfig.FirebaseRemoteConfig{*;}

#Flurry
-keep class com.flurry.** {*;}

# 保留applovin下的所有类及其内部类
-keep class com.applovin.** {*;}
-dontwarn com.applovin.**

#mintegral
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-dontwarn com.mbridge.**
-keepclassmembers class **.R$* { public static final int mbridge*; }

-keep public class com.mbridge.* extends androidx.** { *; }
-keep public class androidx.viewpager.widget.PagerAdapter{*;}
-keep public class androidx.viewpager.widget.ViewPager$OnPageChangeListener{*;}
-keep interface androidx.annotation.IntDef{*;}
-keep interface androidx.annotation.Nullable{*;}
-keep interface androidx.annotation.CheckResult{*;}
-keep interface androidx.annotation.NonNull{*;}
-keep public class androidx.fragment.app.Fragment{*;}
-keep public class androidx.core.content.FileProvider{*;}
-keep public class androidx.core.app.NotificationCompat{*;}
-keep public class androidx.appcompat.widget.AppCompatImageView {*;}
-keep public class androidx.recyclerview.*{*;}
-keep class com.mbridge.msdk.foundation.tools.FastKV{*;}
-keep class com.mbridge.msdk.foundation.tools.FastKV$Builder{*;}


#tradplus
-keep public class com.tradplus.** { *; }
-keep class com.tradplus.ads.** { *; }
-keep class com.max.ads.** { *; }

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

#smaato
-dontwarn com.smaato.**
-keep class com.smaato.**{ *;}
-dontwarn com.iab.**
-keep class com.iab.**{ *;}

#inmobi
-dontwarn com.inmobi.**
-keep class com.inmobi.**{ *;}

#tapjoy
-keep class com.tapjoy.** { *; }
-dontwarn com.tapjoy.**

#chartboost
-keep class com.chartboost.** { *; }

#talking data
-dontwarn com.tendcloud.tenddata.**
-keep class com.tendcloud.** {*;}
-keep public class com.tendcloud.** {  public protected *;}

#bigo
-keep class sg.bigo.** {*;}
-keep class com.iab.omid.** {*;}

#保留v、d、e的log移除w、i的log, 主要是移除firebase的log
-assumenosideeffects class android.util.Log{
    public static *** i(...);
    public static *** w(...);
}