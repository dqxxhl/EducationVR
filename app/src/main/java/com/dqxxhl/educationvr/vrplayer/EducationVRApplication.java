package com.dqxxhl.educationvr.vrplayer;

import android.app.Application;

import com.dqxxhl.educationvr.presenter.ServiceManager;

/**
 * Application初始化
 * Created by hl09287 on 2017/3/16.
 */

public class EducationVRApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ServiceManager.getInstance();
    }
}
