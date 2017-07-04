package com.sd.vr.education.presenter;

/**
 * player控制接口
 * Created by hl09287 on 2017/4/11.
 */

public interface VideoAction {

    void start(String url);//开始播放某个视频

    void play(long position);//播放视频

    void pause(long position);//暂停播放

    void seekTo(long position, int status);//跳转到指定位置0：暂停，1：播放。

    void stop();//停止播放视频

    void shutdown();

}
