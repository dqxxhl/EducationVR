package com.sd.vr.education.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import com.sd.vr.education.presenter.ServiceManager;

/**
 * Created by HL on 2017/5/6.
 */

public class PowerConnectionReceiver extends BroadcastReceiver {
    private static final String TAG = PowerConnectionReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "收到电量信息");
        if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
            //当前剩余电量
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            Log.e(TAG, "收到电量信息:level="+level);
            //电量最大值
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            Log.e(TAG, "收到电量信息:scale="+scale);
            //电量百分比
            float batteryPct = level / (float)scale;

            ServiceManager.getInstance().updateDianliang(batteryPct);
        }

    }
}
