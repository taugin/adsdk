package com.humob.bcsdk;

import java.util.List;

/**
 * Created by Administrator on 2018/2/11.
 */

public abstract class OnPermissionListener {
    public void onPermissionResult(List<String> grantList, List<String> deniedList, boolean goSettings) {
    }

    public void onAllFilesAccessResult(boolean grant) {
    }

    public void onOverlayDrawResult(boolean grant) {
    }

    public void onUsageAccessResult(boolean grant) {
    }

    public void onNotificationListenerResult(boolean grant) {
    }
}
