package com.sd.vr.education.presenter;

/**
 * 首页控制接口
 * Created by hl09287 on 2017/3/27.
 */

public interface ViewAction {

    void stop();



    void showToast(String string);

    void start(String fileId, long size);

    void updateprocess(String process);

    void uodateUI();

    void updateWiFi(int netWorkState);

    void updateDianliang(float batteryPct);

    void shutdown();

}
