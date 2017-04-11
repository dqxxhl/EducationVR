package com.sd.vr.education.presenter;

/**
 * Created by hl09287 on 2017/4/11.
 */

public interface VideoAction {

    void start(String url);//开始播放某个视频

    void play();//播放视频

    void pause();//暂停播放

    void seekTo(long position);//跳转到指定位置

    void stop();//停止播放视频

}
