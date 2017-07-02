package com.sd.vr.education.vrplayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Surface;

import com.sd.vr.education.vrplayer.encrypt.RandomMediaDataSource;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * player封装类
 * Created by hl09287 on 2017/4/14.
 */
public class MediaPlayerWrapper implements MediaPlayer.OnPreparedListener {

    private static final String TAG = MediaPlayerWrapper.class.getName();
    protected MediaPlayer mPlayer;
    private MediaPlayer.OnPreparedListener mPreparedListener;
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PREPARING = 1;
    private static final int STATUS_PREPARED = 2;
    private static final int STATUS_STARTED = 3;
    private static final int STATUS_PAUSED = 4;
    private static final int STATUS_STOPPED = 5;
    private static final int STATUS_COMPLET = 6;
    private int mStatus = STATUS_IDLE;
    private Surface mSurface;

    public void init(){
        mStatus = STATUS_IDLE;
        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                return false;
            }
        });


        enableHardwareDecoding();
    }

    private void enableHardwareDecoding(){
    }

    public void setSurface(Surface surface){
        if (getPlayer() != null){
            getPlayer().setSurface(surface);
        }
        mSurface = surface;
    }

    public void openRemoteFile(String url){
        try {
            mPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaPlayer getPlayer() {
        return mPlayer;
    }

    public void prepare() {
        if (mPlayer == null) return;
        if (mStatus == STATUS_IDLE || mStatus == STATUS_STOPPED){
            mPlayer.prepareAsync();
            mStatus = STATUS_PREPARING;
        }
    }

    public void stop(){
        if (mPlayer == null) return;
        if (mStatus == STATUS_STARTED || mStatus ==  STATUS_PAUSED){
            mPlayer.stop();
            mStatus = STATUS_STOPPED;
        }
    }

    public void pause(){
        if (mPlayer == null) return;
        if ((mPlayer.isPlaying()&& mStatus == STATUS_STARTED) || mStatus == STATUS_COMPLET) {
            mPlayer.pause();
            mStatus = STATUS_PAUSED;
        }
    }

    public void start(){
        if (mPlayer == null) return;
        if (mStatus == STATUS_PREPARED || mStatus == STATUS_PAUSED ||  mStatus == STATUS_COMPLET){
            mPlayer.start();
            mStatus = STATUS_STARTED;
        }

    }

    public void setPreparedListener(MediaPlayer.OnPreparedListener mPreparedListener) {
        this.mPreparedListener = mPreparedListener;
    }

    public void resume() {
        start();
    }

    public void destroy() {
        stop();
        if (mPlayer != null) {
            mPlayer.setSurface(null);
            mPlayer.release();
        }
        mPlayer = null;
    }

    public void seekTo(long l){
        pause();
        mPlayer.seekTo((int) l);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mStatus = STATUS_PREPARED;
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.e(TAG, "播放完成");
                mStatus = STATUS_COMPLET;
            }
        });
        start();
        if (mPreparedListener != null) mPreparedListener.onPrepared(mediaPlayer);
    }
}
