package com.hauyu.adsdk.demo;

import android.content.Context;
import android.util.Log;

import com.hauyu.adsdk.demo.utils.DateUtils;
import com.rabbit.adsdk.http.Http;
import com.rabbit.adsdk.http.OnStringCallback;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RssParser {
    public static final String TAG = "RssParser";

    public static void doUrl(Context context) {
        String url = "http://rss.cnn.com/rss/cnn_topstories.rss";
        Map<String, String> map = new HashMap<>();
        map.put("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.12) Gecko/20101026 Firefox/3.6.12");
        Log.v(TAG, "rss url : " + url);
        Http.get(context).get(url, map, new OnStringCallback() {
            @Override
            public void onSuccess(String content) {
                Log.v(TAG, "content : " + content);
                try {
                    parse(content);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int code, String error) {
                Log.v(TAG, "error : " + error);
            }
        });
    }

    private static void parse(String res) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser xpp = factory.newPullParser();
        InputStream is = new ByteArrayInputStream(res.getBytes());
        xpp.setInput(is, null);
        // xpp.setInput(getInputStream(url), "UTF-8");
        boolean insideItem = false;
// Returns the type of current event: START_TAG, END_TAG, etc..
        int eventType = xpp.getEventType();
        RssItem rssItem = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equalsIgnoreCase("item")) {
                    insideItem = true;
                    rssItem = new RssItem();
                } else if (xpp.getName().equalsIgnoreCase("title")) {
                    if (insideItem) {
                        rssItem.title = xpp.nextText();
                    }
                } else if (xpp.getName().equalsIgnoreCase("link")) {
                    if (insideItem) {
                        rssItem.link = xpp.nextText();
                    }
                } else if (xpp.getName().equalsIgnoreCase("comments")) {
                    if (insideItem) {
                        rssItem.comments = xpp.nextText();
                    }
                } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                    if (insideItem) {
                        rssItem.pubDate = xpp.nextText();
                    }
                } else if (xpp.getName().equalsIgnoreCase("media:content")) {
                    if (insideItem) {
                        rssItem.mediaContent = xpp.getAttributeValue(null, "url");
                    }
                } else if (xpp.getName().equalsIgnoreCase("media:title")) {
                    if (insideItem) {
                        rssItem.mediaTitle = xpp.nextText();
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                insideItem = false;
                Log.v(TAG, "=========================\nrssItem : " + rssItem);
            }
            eventType = xpp.next(); /// move to next element
        }
    }

    static class RssItem {
        public String title;
        public String link;
        public String comments;
        public String pubDate;
        public String mediaTitle;
        public String mediaContent;

        @Override
        public String toString() {
            return "RssItem{" + "\n" +
                    "title='" + title + '\'' + "\n" +
                    ", link='" + link + '\'' + "\n" +
                    ", comments='" + comments + '\'' + "\n" +
                    ", pubDate='" + pubDate + '\'' + "\n" +
                    ", mediaTitle='" + mediaTitle + '\'' + "\n" +
                    ", mediaContent='" + mediaContent + '\'' + "\n" +
                    '}';
        }
    }
}
