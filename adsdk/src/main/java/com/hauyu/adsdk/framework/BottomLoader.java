package com.hauyu.adsdk.framework;

import android.content.Context;
import android.content.Intent;

import com.appub.ads.a.FSA;
import com.appub.ads.a.R;
import com.hauyu.adsdk.AdExtra;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.config.BaseConfig;
import com.hauyu.adsdk.log.Log;
import com.hauyu.adsdk.utils.Utils;

import java.util.Random;

/**
 * Created by Administrator on 2018/12/9.
 */

public abstract class BottomLoader implements AdReceiver.OnTriggerListener {
    
    protected abstract Context getContext();

    protected BaseConfig getBaseConfig() {
        return null;
    }

    protected AdParams generateAdParams() {
        AdParams.Builder builder = new AdParams.Builder();
        builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_DFP, AdExtra.DFP_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_ADX, AdExtra.ADX_MEDIUM_RECTANGLE);

        int layoutId[] = new int[] {R.layout.native_card_full, R.layout.native_card_mix};

        builder.setAdRootLayout(AdExtra.AD_SDK_COMMON, layoutId[new Random(System.currentTimeMillis()).nextInt(layoutId.length)]);
        builder.setAdTitle(AdExtra.AD_SDK_COMMON, R.id.native_title);
        builder.setAdDetail(AdExtra.AD_SDK_COMMON, R.id.native_detail);
        builder.setAdSubTitle(AdExtra.AD_SDK_COMMON, R.id.native_sub_title);
        builder.setAdIcon(AdExtra.AD_SDK_COMMON, R.id.native_icon);
        builder.setAdAction(AdExtra.AD_SDK_COMMON, R.id.native_action_btn);
        builder.setAdCover(AdExtra.AD_SDK_COMMON, R.id.native_image_cover);
        builder.setAdChoices(AdExtra.AD_SDK_COMMON, R.id.native_ad_choices_container);
        builder.setAdMediaView(AdExtra.AD_SDK_COMMON, R.id.native_media_cover);
        AdParams adParams = builder.build();
        return adParams;
    }

    protected void show(String pidName, String source, String adType) {
        try {
            Intent intent = Utils.getIntentByAction(getContext(), getContext().getPackageName() + ".action.AFPICKER");
            if (intent == null) {
                intent = new Intent(getContext(), FSA.class);
            }
            intent.putExtra(Intent.EXTRA_TITLE, pidName);
            intent.putExtra(Intent.EXTRA_TEXT, source);
            intent.putExtra(Intent.EXTRA_TEMPLATE, adType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    protected void hide() {
        try {
            Intent intent = new Intent(getContext().getPackageName() + ".action.FA");
            intent.setPackage(getContext().getPackageName());
            getContext().sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }

    @Override
    public void onAlarm(Context context) {
    }

    @Override
    public void onHomePressed(Context context) {
    }

    @Override
    public void onScreenOn(Context context) {
    }

    @Override
    public void onScreenOff(Context context) {
    }

    @Override
    public void onUserPresent(Context context) {
    }

    @Override
    public void onPowerConnect(Context context, Intent intent) {
    }

    @Override
    public void onPowerDisconnect(Context context, Intent intent) {
    }

    @Override
    public void onBatteryChange(Context context, Intent intent) {
    }

    @Override
    public void onPackageAdded(Context context, Intent intent) {
    }

    @Override
    public void onPackageReplaced(Context context, Intent intent) {
    }

    @Override
    public void onPackageRemoved(Context context, Intent intent) {
    }

    @Override
    public void onNetworkChange(Context context, Intent intent) {
    }
}
