package com.sd.vr.education.vrplayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
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
public class MediaPlayerWrapper implements IMediaPlayer.OnPreparedListener {

    private static final String TAG = MediaPlayerWrapper.class.getName();
    protected IMediaPlayer mPlayer;
    private IjkMediaPlayer.OnPreparedListener mPreparedListener;
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
        mPlayer = new IjkMediaPlayer();
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                return false;
            }
        });


        enableHardwareDecoding();
    }

    private void enableHardwareDecoding(){
        if (mPlayer instanceof IjkMediaPlayer){
            IjkMediaPlayer player = (IjkMediaPlayer) mPlayer;
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 60);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 0);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        }
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

    public void openAssetFile(Context context, String assetPath) {
        try {
            AssetManager am = context.getResources().getAssets();
            final InputStream is = am.open(assetPath);
            mPlayer.setDataSource(new IMediaDataSource() {
                @Override
                public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
                    return is.read(buffer, offset, size);
                }

                @Override
                public long getSize() throws IOException {
                    return is.available();
                }

                @Override
                public void close() throws IOException {
                    is.close();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openLocalFile(String filePath, String key, int len){
        try{
            File file = new File(filePath);
            mPlayer.setDataSource(new RandomMediaDataSource(file, key, len));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public IMediaPlayer getPlayer() {
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

    public void setPreparedListener(IMediaPlayer.OnPreparedListener mPreparedListener) {
        this.mPreparedListener = mPreparedListener;
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        mStatus = STATUS_PREPARED;
        mPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                Log.e(TAG, "播放完成");
                mStatus = STATUS_COMPLET;
            }
        });
        start();
        if (mPreparedListener != null) mPreparedListener.onPrepared(mp);
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
        mPlayer.seekTo(l);
    }
}
