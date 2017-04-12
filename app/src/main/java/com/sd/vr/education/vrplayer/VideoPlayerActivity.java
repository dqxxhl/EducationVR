package com.sd.vr.education.vrplayer;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;
import com.sd.vr.R;
import com.sd.vr.education.presenter.ServiceManager;
import com.sd.vr.education.presenter.VideoAction;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class VideoPlayerActivity extends Activity implements VideoAction {

    private MDVRLibrary mVRLibrary;
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();
    private String url = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏幕显示
        setContentView(R.layout.video_player);
        getdate();
        initVRLibrary();//初始化VR库

        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                if (mVRLibrary != null){
                    mVRLibrary.notifyPlayerChanged();
                }
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
                mVRLibrary.onTextureResize(width, height);
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                mMediaPlayerWrapper.getPlayer().start();
                mMediaPlayerWrapper.getPlayer().setLooping(true);
            }
        });

        ServiceManager.getInstance().bindVideoAction(this);

        playVideo(url);//播放视频
    }

    private void getdate(){
        Intent intent = getIntent();
        url = intent.getStringExtra("URL");

//        url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/F5fly.mp4";
//        url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ceshi.mp4";
//        url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/congo.mp4";
//        url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/yangli.mp4";
    }

    private void playVideo(String url){
        mMediaPlayerWrapper.openRemoteFile(url);
        mMediaPlayerWrapper.prepare();
    }

    private void initVRLibrary(){
        // new instance
        mVRLibrary = MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_GLASS)//采用眼镜沉浸方式播放
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .projectionMode(MDVRLibrary.PROJECTION_MODE_SPHERE)
                .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mMediaPlayerWrapper.setSurface(surface);
                    }
                })
                .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                    @Override
                    public void onNotSupport(int mode) {
                        String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                                ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                        Toast.makeText(VideoPlayerActivity.this, tip, Toast.LENGTH_SHORT).show();
                    }
                })
                .pinchConfig(new MDPinchConfig().setMin(0.5f).setMax(8.0f).setDefaultValue(0.7f))
                .directorFactory(new MD360DirectorFactory() {
                    @Override
                    public MD360Director createDirector(int index) {
                        return MD360Director.builder().setPitch(180).build();
                    }
                })
                .projectionFactory(new CustomProjectionFactory())
                .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(false).setScale(1.2f))
                .build(R.id.surface_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVRLibrary.onResume(this);
        mMediaPlayerWrapper.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVRLibrary.onPause(this);
        mMediaPlayerWrapper.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVRLibrary.onDestroy();
        mMediaPlayerWrapper.destroy();
        ServiceManager.getInstance().unBindVideoAction();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVRLibrary.onOrientationChanged(this);
    }

    @Override
    public void start(String url) {
        playVideo(url);
    }

    @Override
    public void play() {
        if (!mMediaPlayerWrapper.getPlayer().isPlaying()){
            mMediaPlayerWrapper.getPlayer().start();
        }
    }

    @Override
    public void pause() {
        mMediaPlayerWrapper.getPlayer().pause();
    }

    @Override
    public void seekTo(long position) {
        mMediaPlayerWrapper.getPlayer().seekTo(position);
    }

    @Override
    public void stop() {
        mMediaPlayerWrapper.getPlayer().pause();
    }
}
