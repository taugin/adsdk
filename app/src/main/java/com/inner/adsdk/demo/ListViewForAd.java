package com.inner.adsdk.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.simple.mpsdk.AdParams;
import com.simple.mpsdk.AdSdk;
import com.simple.mpsdk.listener.SimpleAdSdkListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2019-3-29.
 */

public class ListViewForAd extends Activity {

    private static final String AD_PLACE_NAME = "banner_and_native";
    private ListView mListView;
    private Map<String, View> mHashMap = null;
    private AdAdapter mAdAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = new ListView(this);
        setContentView(mListView);
        mHashMap = new HashMap<String, View>();
        mAdAdapter = new AdAdapter(this);
        Item item = null;
        for (int index = 0; index < 50; index++) {
            item = new Item();
            if (index == 0 || index == 20 || index == 40) {
                item.type = 0;
            } else {
                item.name = String.valueOf("item_" + index);
                item.type = 1;
            }
            mAdAdapter.add(item);
        }
        mListView.setAdapter(mAdAdapter);
        loadAds();
    }

    private void loadAds() {
        AdParams adParams = null;
        AdParams.Builder builder = new AdParams.Builder();
        adParams = builder.build();
        AdSdk.get(this).loadNative(AD_PLACE_NAME, adParams, new SimpleAdSdkListener() {
            @Override
            public void onLoaded(String pidName, String adType) {
                mAdAdapter.notifyDataSetChanged();
            }
        });
    }

    private class AdAdapter extends ArrayAdapter<Item> {

        public AdAdapter(@NonNull Context context) {
            super(context, 0, new ArrayList<Item>());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Item item = getItem(position);
            if (item.type == 0) {
                FrameLayout adLayout = null;
                if (convertView == null) {
                    convertView = adLayout = new FrameLayout(getContext());
                } else {
                    adLayout = (FrameLayout) convertView;
                }
                adLayout.removeAllViews();
                View adView = mHashMap.get(String.valueOf(position));
                Log.v("taugin", "position : " + position + " , adview : " + adView);
                if (adView == null) {
                    if (AdSdk.get(getContext()).isNativeLoaded(AD_PLACE_NAME)) {
                        AdSdk.get(getContext()).showNative(AD_PLACE_NAME, adLayout);
                        mHashMap.put(String.valueOf(position), adLayout.getChildAt(0));
                    }
                } else {
                    try {
                        if (adView != null) {
                            ViewGroup parentView = (ViewGroup) adView.getParent();
                            if (parentView != null) {
                                parentView.removeView(adView);
                            }
                            adLayout.addView(adView);
                        }
                    } catch (Exception e) {
                        Log.e("taugin", "error : " + e);
                    }
                }
            } else {
                TextView textView = null;
                if (convertView == null) {
                    convertView = textView = (TextView) LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
                } else {
                    textView = (TextView) convertView;
                }
                textView.setText(item.name);
            }
            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Item item = getItem(position);
            if (item != null) {
                return item.type;
            }
            return super.getItemViewType(position);
        }
    }

    private class Item {
        public int type;
        public String name;
    }
}
