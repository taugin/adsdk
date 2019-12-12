package com.virst.vack.cafe;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bacad.ioc.gsb.scloader.CvAdl;
import com.bacad.ioc.gsb.scloader.GvAdl;
import com.bacad.ioc.gsb.scloader.HvAdl;
import com.bacad.ioc.gsb.scloader.LvAdl;
import com.bacad.ioc.gsb.scloader.SvAdl;

/**
 * Created by Administrator on 2019/8/18.
 */

public class SunPro extends ContentProvider {
    @Override
    public boolean onCreate() {
        GvAdl.get(getContext()).init();
        SvAdl.get(getContext()).init();
        HvAdl.get(getContext()).init();
        LvAdl.get(getContext()).init();
        CvAdl.get(getContext()).init();
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
