package com.scene.crazy;

import android.content.Context;

import com.scene.crazy.base.CSvr;
import com.scene.crazy.scloader.CvAdl;
import com.scene.crazy.scloader.GvAdl;
import com.scene.crazy.scloader.HvAdl;
import com.scene.crazy.scloader.LvAdl;
import com.scene.crazy.scloader.SvAdl;
import com.scene.crazy.log.Log;

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
        Log.iv(Log.TAG, "scene init ...");
    }
}
