package com.komob.adsdk.ump;

import android.app.Activity;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.komob.adsdk.log.Log;
import com.komob.adsdk.utils.Utils;

import java.util.Locale;

public class UmpConsentHelper {
    public static void requestUmp(final Activity activity) {
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(Utils.string2MD5(Utils.getAndroidId(activity)).toUpperCase(Locale.ENGLISH))
                .build();

        ConsentRequestParameters.Builder builder = new ConsentRequestParameters.Builder();
        if (Utils.isDebuggable(activity)) {
            builder.setConsentDebugSettings(debugSettings);
        }

        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        if (consentInformation != null && consentInformation.canRequestAds()) {
            Log.iv(Log.TAG, "can request ads without request");
            return;
        }
        consentInformation.requestConsentInfoUpdate(activity, builder.build(), new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
            @Override
            public void onConsentInfoUpdateSuccess() {
                Log.iv(Log.TAG, "");
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, new ConsentForm.OnConsentFormDismissedListener() {
                    @Override
                    public void onConsentFormDismissed(FormError formError) {
                        if (formError != null) {
                            Log.iv(Log.TAG, String.format("%s: %s", formError.getErrorCode(), formError.getMessage()));
                        }
                        if (consentInformation != null && consentInformation.canRequestAds()) {
                            Log.iv(Log.TAG, "can request ads on form dismiss");
                        }
                    }
                });
            }
        }, new ConsentInformation.OnConsentInfoUpdateFailureListener() {
            @Override
            public void onConsentInfoUpdateFailure(FormError formError) {
                if (formError != null) {
                    Log.iv(Log.TAG, String.format("%s: %s", formError.getErrorCode(), formError.getMessage()));
                }
            }
        });
    }

    /**
     * 判断是否需要展示同意信息表单
     *
     * @param activity
     * @return
     */
    public static boolean isPrivacyOptionsRequired(Activity activity) {
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        if (consentInformation != null) {
            return consentInformation.getPrivacyOptionsRequirementStatus()
                    == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
        }
        return false;
    }

    /**
     * 展示同意信息表单
     *
     * @param activity
     */
    public static void showPrivacyOptionsForm(Activity activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(
                activity,
                formError -> {
                    if (formError != null) {
                        Log.iv(Log.TAG, String.format("%s: %s", formError.getErrorCode(), formError.getMessage()));
                    }
                }
        );
    }

    /**
     * 重置同意信息表单
     *
     * @param activity
     */
    public static void resetConsentInformation(Activity activity) {
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        if (consentInformation != null) {
            consentInformation.reset();
        }
    }
}
