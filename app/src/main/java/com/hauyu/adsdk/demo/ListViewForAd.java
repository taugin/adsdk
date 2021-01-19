package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.rabbit.adsdk.AdExtra;
import com.rabbit.adsdk.AdParams;
import com.rabbit.adsdk.AdSdk;

import java.util.ArrayList;

/**
 * Created by Administrator on 2019-3-29.
 */

public class ListViewForAd extends Activity {

    private static final String AD_PLACE_NAME = "banner_and_native";
    private ListView mListView;
    private AdAdapter mAdAdapter;
    private AdParams adParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = new ListView(this);
        setContentView(mListView);
        mAdAdapter = new AdAdapter(this);
        Item item = null;
        for (int index = 0; index < 50; index++) {
            item = new Item();
            if (index % 10 == 0) {
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
        AdParams.Builder builder = new AdParams.Builder();
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_SMALL);
        adParams = builder.build();
        AdSdk.get(this).loadAdView(AD_PLACE_NAME, adParams);
    }

    private class AdAdapter extends ArrayAdapter<Item> {

        public AdAdapter(Context context) {
            super(context, 0, new ArrayList<Item>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = getItem(position);
            if (item.type == 0) {
                FrameLayout adLayout = null;
                if (convertView == null) {
                    convertView = adLayout = new FrameLayout(getContext());
                } else {
                    adLayout = (FrameLayout) convertView;
                }
                if (adLayout.getChildCount() <= 0) {
                    if (AdSdk.get(getContext()).isAdViewLoaded(AD_PLACE_NAME)) {
                        AdSdk.get(getContext()).showAdView(AD_PLACE_NAME, adLayout);
                    }
                }
                if (!AdSdk.get(getContext()).isAdViewLoaded(AD_PLACE_NAME)) {
                    AdSdk.get(getContext()).loadAdView(AD_PLACE_NAME, adParams);
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
