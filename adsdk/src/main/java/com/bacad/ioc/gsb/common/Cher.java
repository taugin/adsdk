package com.bacad.ioc.gsb.common;

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

import com.bacad.ioc.gsb.scloader.CvAdl;
import com.bacad.ioc.gsb.scpolicy.CvPcy;
import com.dock.vost.moon.IAdvance;
import com.dock.vost.moon.R;
import com.hauyu.adsdk.AdExtra;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.listener.SimpleAdSdkListener;
import com.hauyu.adsdk.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018-11-21.
 */

public class Cher implements View.OnClickListener {
    private Activity mActivity;
    private ImageView chargeCancel, chargeMore;
    private IAdvance.Dot speedChargeProgress, continuousChargeProgress;
    private IAdvance.Blank speedBlink, continuousBlink, trickleBlink;
    private TextView speedText, continuousText, trickleText;
    private TextView timeInfo, batteryLevel;
    private Timer timer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ViewGroup mAdContainer;

    public Cher(Activity activity) {
        mActivity = activity;
    }

    public void showChargeView(final String placeName) {
        if (mActivity == null) {
            return;
        }
        if (Utils.isScreenLocked(mActivity)) {
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        mActivity.setContentView(R.layout.had_card_ch);
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
                fillAd(placeName);
            }
        }, 500);

        if (CvPcy.get(mActivity).allowDisableMonitor()) {
            chargeMore.setVisibility(View.VISIBLE);
        } else {
            chargeMore.setVisibility(View.GONE);
        }
    }

    private AdParams getParams() {
        AdParams adParams = null;
        try {
            adParams = ((IAdvance)mActivity).getCvParams();
        } catch (Exception | Error e) {
        }
        if (adParams == null) {
            AdParams.Builder builder = new AdParams.Builder();
            builder.setBannerSize(AdExtra.AD_SDK_COMMON, AdExtra.COMMON_MEDIUM_RECTANGLE);
            builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_SMALL);
            adParams = builder.build();
        }
        return adParams;
    }

    private void fillAd(String placeName) {
        AdParams adParams = getParams();
        AdSdk.get(mActivity).loadAdView(placeName, adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String source, String adType) {
                if (mActivity != null && !mActivity.isFinishing()) {
                    AdSdk.get(mActivity).showAdView(pidName, getParams(), mAdContainer);
                    onCtShowing(mAdContainer);
                }
            }
        });
    }

    private void onCtShowing(View containerView) {
        try {
            ((IAdvance)mActivity).onCvShowing(containerView);
        } catch (Exception | Error e) {
        }
    }

    private void update() {
        int percent = CvAdl.BatteryInfo.getPercent();
        updateBatteryLevel(percent);
        if (CvAdl.BatteryInfo.isCharging()) {
            if (percent < 80) {
                speedBlink.start();
                continuousBlink.setBackground();
                continuousBlink.stop();
                trickleBlink.setBackground();
                trickleBlink.stop();
                speedText.setEnabled(true);
                continuousText.setEnabled(false);
                trickleText.setEnabled(false);
                int chargingTimeInSecond = CvAdl.BatteryInfo.estimateRemainChargingTime();
                int hours = chargingTimeInSecond / 3600;
                int minutes = (chargingTimeInSecond - hours * 3600) / 60;
                updateTimeInfo(mActivity.getString(R.string.had_cm_label_charging), hours, minutes);
            } else if (percent >= 80 && percent < 100) {
                speedBlink.stop();
                speedBlink.setAlpha();
                continuousBlink.start();
                trickleBlink.stop();
                trickleBlink.setBackground();
                speedText.setEnabled(false);
                continuousText.setEnabled(true);
                trickleText.setEnabled(false);
                int chargingTimeInSecond = CvAdl.BatteryInfo.estimateRemainChargingTime();
                int hours = chargingTimeInSecond / 3600;
                int minutes = (chargingTimeInSecond - hours * 3600) / 60;
                updateTimeInfo(mActivity.getString(R.string.had_cm_label_charging), hours, minutes);
            } else {
                speedBlink.stop();
                speedBlink.setAlpha();
                continuousBlink.stop();
                continuousBlink.setAlpha();
                speedText.setEnabled(false);
                continuousText.setEnabled(false);

                trickleBlink.stop();
                trickleBlink.setAlpha();
                trickleText.setEnabled(true);
                updateTimeInfo(mActivity.getString(R.string.had_cm_complete));
            }
            updateProgress(percent);
        } else {
            speedBlink.stop();
            speedBlink.setBackground();
            continuousBlink.stop();
            continuousBlink.setBackground();
            trickleBlink.stop();
            trickleBlink.setBackground();
            speedChargeProgress.setMaxWidth(-1);
            continuousChargeProgress.setMaxWidth(-1);
            speedText.setEnabled(false);
            continuousText.setEnabled(false);
            trickleText.setEnabled(false);
            int timeInMinutes = CvAdl.BatteryInfo.estimateRemainBatteryTime();
            int hours = timeInMinutes / 60;
            int minutes = timeInMinutes % 60;
            updateTimeInfo(mActivity.getString(R.string.had_cm_label_standby), hours, minutes);
        }
    }

    private void updateProgress(int percent) {
        if (percent < 80) {
            if (0 <= percent && percent < 27) {
                speedChargeProgress.setMaxWidth(0);
            } else if (27 <= percent && percent < 54) {
                speedChargeProgress.setMaxWidth(1);
            } else {
                speedChargeProgress.setMaxWidth(2);
            }
            continuousChargeProgress.setMaxWidth(-1);
        } else {
            speedChargeProgress.setMaxWidth(2);
            if (80 <= percent && percent < 87) {
                continuousChargeProgress.setMaxWidth(0);
            } else if (87 <= percent && percent < 94) {
                continuousChargeProgress.setMaxWidth(1);
            } else {
                continuousChargeProgress.setMaxWidth(2);
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
            sb.append(mActivity.getResources().getString(R.string.had_cm_span_h));
            sb.setSpan(hourLabelSpan, start, sb.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        start = sb.length();
        sb.append(String.valueOf(minutes));
        sb.setSpan(minNumSpan, start, sb.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        start = sb.length();
        sb.append(mActivity.getResources().getString(R.string.had_cm_span_m));
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
        if (id == R.id.ad_cm_view_cancel) {
            if (mActivity != null) {
                mActivity.finish();
            }
        } else if (id == R.id.ad_cm_view_more) {
            showDialog();
        }
    }

    private void showDialog() {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(mActivity);
        normalDialog.setTitle(R.string.had_cm_dialog_title);
        normalDialog.setMessage(R.string.had_cm_dialog_message);
        normalDialog.setPositiveButton(R.string.had_cm_dialog_cancel, null);
        normalDialog.setNegativeButton(R.string.had_cm_dialog_disable,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CvPcy.get(mActivity).disableMonitor();
                        finishActivityDelay();
                    }
                });
        normalDialog.show();
    }

    private void finishActivityDelay() {
        if (handler != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mActivity != null) {
                        mActivity.finish();
                    }
                }
            }, 500);
        } else {
            if (mActivity != null) {
                mActivity.finish();
            }
        }
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
