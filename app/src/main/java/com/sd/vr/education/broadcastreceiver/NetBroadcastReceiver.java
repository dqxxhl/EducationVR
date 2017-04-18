package com.sd.vr.education.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.sd.vr.education.presenter.ServiceManager;
import com.sd.vr.education.utils.Utils;

/**
 * Created by Administrator on 2017/4/17.
 */

public class NetBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = NetBroadcastReceiver.class.getName();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            int netWorkState = Utils.getNetWorkState(context);
            // 接口回调传过去状态的类型
            ServiceManager.getInstance().onNetChange(netWorkState);
        }
    }
}
