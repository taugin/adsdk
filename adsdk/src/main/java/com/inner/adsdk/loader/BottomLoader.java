package com.inner.adsdk.loader;

import android.content.Context;
import android.content.Intent;

import com.appub.ads.a.FSA;
import com.appub.ads.a.R;
import com.inner.adsdk.AdExtra;
import com.inner.adsdk.AdParams;
import com.inner.adsdk.log.Log;
import com.inner.adsdk.policy.BasePolicy;
import com.inner.adsdk.utils.Utils;

import java.util.Random;

/**
 * Created by Administrator on 2018/12/9.
 */

public abstract class BottomLoader<Config, Policy> implements AdReceiver.OnTriggerListener {

    protected Config mConfig;

    protected Policy mPolicy;
    
    protected abstract Context getContext();

    public Config getConfig() {
        return null;
    }

    public Config createConfig() {
        return null;
    }

    public Policy createPolicy() {
        return null;
    }

    public void reportShowing() {
        try {
            ((BasePolicy) mPolicy).reportShowing(true);
        } catch (Exception e) {
        }
    }

    protected AdParams generateAdParams() {
        AdParams.Builder builder = new AdParams.Builder();
        builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_DFP, AdExtra.DFP_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_FACEBOOK, AdExtra.FB_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_ADX, AdExtra.ADX_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_DSPMOB, AdExtra.DSPMOB_MEDIUM_RECTANGLE);

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
    public void onPackageRemoved(Context context, Intent intent) {
    }

    @Override
    public void onNetworkChange(Context context, Intent intent) {
    }
}
