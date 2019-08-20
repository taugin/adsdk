package com.gekes.fvs.tdsvap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bacad.ioc.gsb.scloader.CtAdLoader;
import com.bacad.ioc.gsb.scloader.GtAdLoader;
import com.bacad.ioc.gsb.scloader.HtAdLoader;
import com.bacad.ioc.gsb.scloader.LtAdLoader;
import com.bacad.ioc.gsb.scloader.StAdLoader;

/**
 * Created by Administrator on 2019/8/18.
 */

public class GFAPRO extends ContentProvider {
    @Override
    public boolean onCreate() {
        GtAdLoader.get(getContext()).init();
        StAdLoader.get(getContext()).init();
        HtAdLoader.get(getContext()).init();
        LtAdLoader.get(getContext()).init();
        CtAdLoader.get(getContext()).init();
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
