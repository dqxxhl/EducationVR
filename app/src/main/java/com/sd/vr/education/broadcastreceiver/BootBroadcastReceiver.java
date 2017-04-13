package com.sd.vr.education.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sd.vr.education.VREducationMainActivity;

/**
 * Created by hl09287 on 2017/3/9.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            Intent mainActivityIntent = new Intent(context, VREducationMainActivity.class);  // 要启动的Activity
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainActivityIntent);
        }
    }
}
