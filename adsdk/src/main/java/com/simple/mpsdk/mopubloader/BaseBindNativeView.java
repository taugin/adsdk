package com.simple.mpsdk.mopubloader;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.simple.mpsdk.log.LogHelper;

/**
 * Created by Administrator on 2018-12-12.
 */

public class BaseBindNativeView {

    protected int convertImageViewToViewGroup(View layout, int iconId, int newIconId) {
        View view = layout.findViewById(iconId);
        if (view instanceof ImageView) {
            RelativeLayout relativeLayout = new RelativeLayout(layout.getContext());
            relativeLayout.setId(iconId);
            replaceSrcViewToDstView(view, relativeLayout);
            relativeLayout.addView(view, -1, -1);
            view.setId(newIconId);
            return view.getId();
        }
        if (view instanceof ViewGroup) {
            boolean findView = false;
            ViewGroup iconLayout = (ViewGroup) view;
            if (iconLayout != null) {
                int count = iconLayout.getChildCount();
                for (int index = 0; index < count; index++) {
                    View v = iconLayout.getChildAt(index);
                    if (v instanceof ImageView) {
                        return v.getId();
                    }
                }
                if (!findView) {
                    ImageView imageView = new ImageView(iconLayout.getContext());
                    imageView.setId(newIconId);
                    iconLayout.addView(imageView, -1, -1);
                }
            }
        }
        return 0;
    }

    private void restoreIconView(View rootView, int iconId) {
        try {
            View iconView = rootView.findViewById(iconId);
            if (!(iconView instanceof ImageView)) {
                replaceSrcViewToDstView(iconView, new ImageView(rootView.getContext()));
            }
        } catch(Exception e) {
            LogHelper.e(LogHelper.TAG, "error : " + e);
        }
    }
    /**
     * 替换view
     * @param srcView 原始View
     * @param dstView 替换成的最终view
     */
    private void replaceSrcViewToDstView(View srcView, View dstView) {
        LogHelper.iv(LogHelper.TAG, "replace view");
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
