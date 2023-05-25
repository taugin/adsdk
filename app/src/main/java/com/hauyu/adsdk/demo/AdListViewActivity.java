package com.hauyu.adsdk.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hauyu.adsdk.AdExtra;
import com.hauyu.adsdk.AdParams;
import com.hauyu.adsdk.AdSdk;
import com.hauyu.adsdk.OnAdSdkListener;
import com.hauyu.adsdk.SimpleAdSdkListener;
import com.hauyu.adsdk.Utils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2019-3-29.
 */

public class AdListViewActivity extends Activity {

    private static final String AD_PLACE_NAME = "native_admob";
    private ListView mListView;
    private AdAdapter mAdAdapter;
    private AdParams adParams;
    private int mScrollState = ListView.OnScrollListener.SCROLL_STATE_IDLE;
    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = new ListView(this);
        setContentView(mListView);
        mAdAdapter = new AdAdapter(this);
        Item item = null;
        for (int index = 0; index < 50; index++) {
            if (index > 0 && index % 3 == 0) {
                item = new Item();
                item.type = 0;
                mAdAdapter.add(item);
            }
            item = new Item();
            item.name = String.valueOf("item_" + index);
            item.type = 1;
            mAdAdapter.add(item);
        }
        mListView.setAdapter(mAdAdapter);
        loadAds();
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mScrollState = scrollState;
                Log.v(Log.TAG, "mScrollState : " + mScrollState);
                if (mScrollState == ListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    mAdAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    private void loadAds() {
        AdParams.Builder builder = new AdParams.Builder();
        builder.setAdCardStyle(AdExtra.AD_SDK_COMMON, AdExtra.NATIVE_CARD_SMALL);
        adParams = builder.build();
        AdSdk.get(this).loadAdView(AD_PLACE_NAME, adParams, onAdSdkListener);
    }

    private OnAdSdkListener onAdSdkListener = new SimpleAdSdkListener() {
        @Override
        public void onLoaded(String placeName, String source, String adType, String pid) {
            if (!atomicBoolean.getAndSet(true)) {
                mAdAdapter.notifyDataSetChanged();
            }
        }
    };

    private class AdAdapter extends ArrayAdapter<Item> {

        public AdAdapter(Context context) {
            super(context, 0, new ArrayList<Item>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.v(Log.TAG, "position : " + position);
            Item item = getItem(position);
            if (item.type == 0) {
                FrameLayout adLayout = null;
                if (convertView == null) {
                    convertView = adLayout = new FrameLayout(getContext());
                } else {
                    adLayout = (FrameLayout) convertView;
                }
                if (adLayout.getChildCount() <= 0 && (position == 3 || mScrollState == ListView.OnScrollListener.SCROLL_STATE_IDLE)) {
                    if (AdSdk.get(getContext()).isAdViewLoaded(AD_PLACE_NAME)) {
                        AdSdk.get(getContext()).showAdView(AD_PLACE_NAME, adLayout);
                    }
                }
                if (!AdSdk.get(getContext()).isAdViewLoaded(AD_PLACE_NAME)) {
                    AdSdk.get(getContext()).loadAdView(AD_PLACE_NAME, adParams, onAdSdkListener);
                }
            } else {
                TextView textView = null;
                if (convertView == null) {
                    convertView = textView = (TextView) LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
                    textView.setLayoutParams(new ViewGroup.LayoutParams(-1, Utils.dp2px(getContext(), 120)));
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
