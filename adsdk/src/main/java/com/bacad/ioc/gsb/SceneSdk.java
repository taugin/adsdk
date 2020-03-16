package com.bacad.ioc.gsb;

import android.content.Context;

import com.bacad.ioc.gsb.base.CSvr;
import com.bacad.ioc.gsb.scloader.CvAdl;
import com.bacad.ioc.gsb.scloader.GvAdl;
import com.bacad.ioc.gsb.scloader.HvAdl;
import com.bacad.ioc.gsb.scloader.LvAdl;
import com.bacad.ioc.gsb.scloader.SvAdl;

/**
 * Created by Administrator on 2019-12-18.
 */

public class SceneSdk {
    public static void init(Context context) {
        if (context == null) {
            return;
        }
        CSvr.get(context).init();
        GvAdl.get(context.getApplicationContext()).init();
        SvAdl.get(context.getApplicationContext()).init();
        HvAdl.get(context.getApplicationContext()).init();
        LvAdl.get(context.getApplicationContext()).init();
        CvAdl.get(context.getApplicationContext()).init();
    }
}
