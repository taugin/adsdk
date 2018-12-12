package com.hauyu.adsdk.adloader.base;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hauyu.adsdk.log.Log;


/**
 * Created by Administrator on 2018-12-12.
 */

public class BaseBindNativeView {

    protected void restoreIconView(View rootView, String source, int iconId) {
        try {
            View iconView = rootView.findViewById(iconId);
            if (!(iconView instanceof ImageView)) {
                replaceSrcViewToDstView(iconView, new ImageView(rootView.getContext()));
            }
        } catch(Exception e) {
            Log.e(Log.TAG, "error : " + e);
        }
    }
    /**
     * 替换view
     * @param srcView 原始View
     * @param dstView 替换成的最终view
     * @return
     */
    private void replaceSrcViewToDstView(View srcView, View dstView) {
        Log.v(Log.TAG, "replace view for correct type");
        if (srcView != null && dstView != null) {
            dstView.setId(srcView.getId());
            ViewGroup.LayoutParams srcParams = srcView.getLayoutParams();
            android.widget.RelativeLayout.LayoutParams dstParams = new android.widget.RelativeLayout.LayoutParams(srcParams.width, srcParams.height);
            if (srcParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams)srcParams;
                dstParams.setMargins(marginParams.leftMargin, marginParams.topMargin, marginParams.rightMargin, marginParams.bottomMargin);
            }
            if (srcParams instanceof android.widget.RelativeLayout.LayoutParams) {
                android.widget.RelativeLayout.LayoutParams mainImageViewRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams)srcParams;
                int[] rules = mainImageViewRelativeLayoutParams.getRules();

                for(int i = 0; i < rules.length; ++i) {
                    dstParams.addRule(i, rules[i]);
                }
            }
            ViewGroup viewGroup = (ViewGroup) srcView.getParent();
            if (viewGroup != null) {
                int index = viewGroup.indexOfChild(srcView);
                viewGroup.removeView(srcView);
                viewGroup.addView(dstView, index, dstParams);
            }
        }
    }
}
