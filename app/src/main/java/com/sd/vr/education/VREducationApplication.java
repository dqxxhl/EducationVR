package com.sd.vr.education;

import android.app.Application;
import android.util.Log;

import com.sd.vr.education.presenter.ServiceManager;

/**
 * Application初始化
 * Created by hl09287 on 2017/3/16.
 */

public class VREducationApplication extends Application {

    public static VREducationApplication application;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("VREducationApplication","VREducationApplication:onCreate()");
        ServiceManager.getInstance().initContext(this);

        application = this;
    }

    public static VREducationApplication getInstance(){
        return application;
    }

}
