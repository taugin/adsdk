package com.hauyu.adsdk.framework;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.appub.ads.a.FSA;
import com.hauyu.adsdk.AdExtra;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.R;
import com.hauyu.adsdk.constant.Constant;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.policy.BsPolicy;
import com.hauyu.adsdk.policy.CtPolicy;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018-11-21.
 */

public class ChargeWrapper implements View.OnClickListener {
    private Activity mActivity;
    private ImageView chargeCancel, chargeMore;
    private FSA.DotProgress speedChargeProgress, continuousChargeProgress;
    private FSA.BlinkImageView speedBlink, continuousBlink, trickleBlink;
    private TextView speedText, continuousText, trickleText;
    private TextView timeInfo, batteryLevel;
    private Timer timer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ViewGroup mAdContainer;

    public ChargeWrapper(Activity activity) {
        mActivity = activity;
    }

    public void showChargeView() {
        mActivity.setContentView(R.layout.native_card_cm);
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        chargeCancel = mActivity.findViewById(R.id.ad_cm_view_cancel);
        chargeCancel.setOnClickListener(this);
        chargeMore = mActivity.findViewById(R.id.ad_cm_view_more);
        chargeMore.setOnClickListener(this);

        mAdContainer = mActivity.findViewById(R.id.ad_cm_ad_layout);
        speedChargeProgress = mActivity.findViewById(R.id.ad_cm_dot_progress_1);
        continuousChargeProgress = mActivity.findViewById(R.id.ad_cm_dot_progress_2);
        speedBlink = mActivity.findViewById(R.id.ad_cm_speed_indicator);
        continuousBlink = mActivity.findViewById(R.id.ad_cm_continuous_indicator);
        trickleBlink = mActivity.findViewById(R.id.ad_cm_trickle_indicator);
        speedText = mActivity.findViewById(R.id.ad_cm_text_speed);
        continuousText = mActivity.findViewById(R.id.ad_cm_text_continuous);
        trickleText = mActivity.findViewById(R.id.ad_cm_text_trickle);
        timeInfo = mActivity.findViewById(R.id.ad_cm_battery_time_info);
        batteryLevel = mActivity.findViewById(R.id.ad_cm_battery_level);

        mAdContainer.setVisibility(View.INVISIBLE);
        update();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fillAd();
            }
        }, 500);

        if (CtPolicy.get(mActivity).allowDisableMonitor()) {
            chargeMore.setVisibility(View.VISIBLE);
        } else {
            chargeMore.setVisibility(View.GONE);
        }
    }

    private void fillAd() {
        AdParams.Builder builder = new AdParams.Builder();
        builder.setBannerSize(AdExtra.AD_SDK_ADMOB, AdExtra.ADMOB_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_DFP, AdExtra.DFP_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_FACEBOOK, AdExtra.FB_MEDIUM_RECTANGLE);
        builder.setBannerSize(AdExtra.AD_SDK_ADX, AdExtra.ADX_MEDIUM_RECTANGLE);
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_SMALL);
        AdParams adParams = builder.build();
        AdSdk.get(mActivity).loadAdView(Constant.CTPLACE_OUTER_NAME, adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                if (mActivity != null && !mActivity.isFinishing()) {
                    AdSdk.get(mActivity).showAdView(pidName, mAdContainer);
                }
            }
        });
    }

    private void update() {
        BsPolicy bsPolicy = BsPolicy.get();
        if(bsPolicy == null){
            return;
        }
        int percent = bsPolicy.getPercent();
        updateBatteryLevel(percent);
        if (bsPolicy.isCharging()) {
            if (percent < 80) {
                speedBlink.startBlink();
                continuousBlink.halftrans();
                continuousBlink.stopBlink();
                trickleBlink.halftrans();
                trickleBlink.stopBlink();
                speedText.setEnabled(true);
                continuousText.setEnabled(false);
                trickleText.setEnabled(false);
                int chargingTimeInSecond = bsPolicy.estimateRemainChargingTime();
                int hours = chargingTimeInSecond / 3600;
                int minutes = (chargingTimeInSecond - hours * 3600) / 60;
                updateTimeInfo(mActivity.getString(R.string.ad_cm_label_charging), hours, minutes);
            } else if (percent >= 80 && percent < 100) {
                speedBlink.stopBlink();
                speedBlink.solid();
                continuousBlink.startBlink();
                trickleBlink.stopBlink();
                trickleBlink.halftrans();
                speedText.setEnabled(false);
                continuousText.setEnabled(true);
                trickleText.setEnabled(false);
                int chargingTimeInSecond = bsPolicy.estimateRemainChargingTime();
                int hours = chargingTimeInSecond / 3600;
                int minutes = (chargingTimeInSecond - hours * 3600) / 60;
                updateTimeInfo(mActivity.getString(R.string.ad_cm_label_charging), hours, minutes);
            } else {
                speedBlink.stopBlink();
                speedBlink.solid();
                continuousBlink.stopBlink();
                continuousBlink.solid();
                speedText.setEnabled(false);
                continuousText.setEnabled(false);

                trickleBlink.stopBlink();
                trickleBlink.solid();
                trickleText.setEnabled(true);
                updateTimeInfo(mActivity.getString(R.string.ad_cm_complete));
            }
            updateProgress(percent);
        } else {
            speedBlink.stopBlink();
            speedBlink.halftrans();
            continuousBlink.stopBlink();
            continuousBlink.halftrans();
            trickleBlink.stopBlink();
            trickleBlink.halftrans();
            speedChargeProgress.setStep(-1);
            continuousChargeProgress.setStep(-1);
            speedText.setEnabled(false);
            continuousText.setEnabled(false);
            trickleText.setEnabled(false);
            int timeInMinutes = bsPolicy.estimateRemainBatteryTime();
            int hours = timeInMinutes / 60;
            int minutes = timeInMinutes % 60;
            updateTimeInfo(mActivity.getString(R.string.ad_cm_label_standby), hours, minutes);
        }
    }

    private void updateProgress(int percent) {
        if (percent < 80) {
            if (0 <= percent && percent < 27) {
                speedChargeProgress.setStep(0);
            } else if (27 <= percent && percent < 54) {
                speedChargeProgress.setStep(1);
            } else {
                speedChargeProgress.setStep(2);
            }
            continuousChargeProgress.setStep(-1);
        } else {
            speedChargeProgress.setStep(2);
            if (80 <= percent && percent < 87) {
                continuousChargeProgress.setStep(0);
            } else if (87 <= percent && percent < 94) {
                continuousChargeProgress.setStep(1);
            } else {
                continuousChargeProgress.setStep(2);
            }
        }
    }

    private void updateTimeInfo(String label) {
        TextAppearanceSpan labelSpan =
                new TextAppearanceSpan(mActivity, R.style.AdCMCompleteStyle);

        SpannableStringBuilder sb = new SpannableStringBuilder();
        int start = sb.length();
        sb.append(label);
        sb.setSpan(labelSpan, start, sb.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        timeInfo.setText(sb);
    }

    private void updateTimeInfo(String label, int hours, int minutes) {
        TextAppearanceSpan labelSpan =
                new TextAppearanceSpan(mActivity, R.style.AdCMStageLabelStyle);
        TextAppearanceSpan hourNumSpan =
                new TextAppearanceSpan(mActivity, R.style.AdCMTimeStyle);
        TextAppearanceSpan minNumSpan =
                new TextAppearanceSpan(mActivity, R.style.AdCMTimeStyle);
        TextAppearanceSpan hourLabelSpan =
                new TextAppearanceSpan(mActivity, R.style.AdCMUnitStyle);
        TextAppearanceSpan minLabelSpan =
                new TextAppearanceSpan(mActivity, R.style.AdCMUnitStyle);

        SpannableStringBuilder sb = new SpannableStringBuilder();
        int start = sb.length();
        sb.append(label);
        sb.setSpan(labelSpan, start, sb.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        if (hours > 0) {
            start = sb.length();
            sb.append(String.valueOf(hours));
            sb.setSpan(hourNumSpan, start, sb.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

            start = sb.length();
            sb.append(mActivity.getResources().getString(R.string.ad_cm_span_h));
            sb.setSpan(hourLabelSpan, start, sb.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        start = sb.length();
        sb.append(String.valueOf(minutes));
        sb.setSpan(minNumSpan, start, sb.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        start = sb.length();
        sb.append(mActivity.getResources().getString(R.string.ad_cm_span_m));
        sb.setSpan(minLabelSpan, start, sb.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        timeInfo.setText(sb);
    }

    private void updateBatteryLevel(int percent) {
        TextAppearanceSpan numSpan =
                new TextAppearanceSpan(mActivity, R.style.AdCMBatteryPercentNum);
        TextAppearanceSpan percentSpan =
                new TextAppearanceSpan(mActivity, R.style.AdCMBatteryPercent);

        SpannableStringBuilder sb = new SpannableStringBuilder();
        int start = sb.length();
        sb.append(String.valueOf(percent));
        sb.setSpan(numSpan, start, sb.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        start = sb.length();
        sb.append("%");
        sb.setSpan(percentSpan, start, sb.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        batteryLevel.setText(sb);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.ad_cm_view_cancel){
            if (mActivity != null) {
                mActivity.finish();
            }
        }else if(id == R.id.ad_cm_view_more){
            showDialog();
        }
    }

    private void showDialog(){
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(mActivity);
        normalDialog.setTitle(R.string.ad_cm_dialog_title);
        normalDialog.setMessage(R.string.ad_cm_dialog_message);
        normalDialog.setPositiveButton(R.string.ad_cm_dialog_cancel, null);
        normalDialog.setNegativeButton(R.string.ad_cm_dialog_disable,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CtPolicy.get(mActivity).disableMonitor();
                        if (mActivity != null) {
                            mActivity.finish();
                        }
                    }
                });
        normalDialog.show();
    }

    public void onResume() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (handler != null) {
                    handler.post(updateDateRunnable);
                }
            }
        }, 0, 1000);
    }

    public void onPause() {
        if (timer != null) {
            timer.cancel();
        }
        if (handler != null) {
            handler.removeCallbacks(updateDateRunnable);
        }
    }

    private Runnable updateDateRunnable = new Runnable() {
        @Override
        public void run() {
            update();
        }
    };

    public void onDestroy() {
    }
}
