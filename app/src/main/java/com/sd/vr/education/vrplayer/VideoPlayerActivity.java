package com.sd.vr.education.vrplayer;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360DirectorFactory;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.BarrelDistortionConfig;
import com.asha.vrlib.model.MDPinchConfig;
import com.sd.vr.R;
import com.sd.vr.education.entity.VideoFile;
import com.sd.vr.education.presenter.FilesManager;
import com.sd.vr.education.presenter.ServiceManager;
import com.sd.vr.education.presenter.VideoAction;
import com.sd.vr.education.utils.DatabaseManager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 视频播放页面
 * Created by hl09287 on 2017/4/14.
 */
public class VideoPlayerActivity extends Activity implements VideoAction {

    private static final String TAG =  VideoPlayerActivity.class.getName();
    private MDVRLibrary mVRLibrary;
    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();
    private Button b;
    private Button b1;
    private Button b2;
    private Button end;
    private Button next;
    private Handler handler = new Handler();
    private String fileId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏幕显示
        setContentView(R.layout.video_player);
        b= (Button) findViewById(R.id.text1);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ServiceManager.getInstance().requestProgress();
                play(0);
            }
        });

        b1= (Button) findViewById(R.id.text2);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pause(1);
            }
        });

        b2= (Button) findViewById(R.id.text3);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekTo(2000, 1);
            }
        });

        end = (Button) findViewById(R.id.end);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start("F5fly.mp4");
            }
        });

        getdate();
        initVRLibrary();//初始化VR库

        initMediaPlayer();

        ServiceManager.getInstance().bindVideoAction(this);

        playVideo(fileId);//播放视频
    }


    private void initMediaPlayer(){
        mMediaPlayerWrapper.init();
        mMediaPlayerWrapper.setPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.e(TAG,"播放器准备就绪");
                if (mVRLibrary != null){
                    mVRLibrary.notifyPlayerChanged();
                }
                mMediaPlayerWrapper.seekTo(0);
                // 播放器准备就绪，向服务器请求目前的播放进度
                Log.e(TAG,"向服务器请求目前的播放进度");
                ServiceManager.getInstance().requestProgress();
            }
        });

        mMediaPlayerWrapper.getPlayer().setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                mVRLibrary.onTextureResize(i, i1);
            }
        });

    }

    private void getdate(){
        Intent intent = getIntent();
        fileId =  intent.getStringExtra("videoFile");


//        url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/F5fly.mp4";
//        url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ceshi.mp4";
//        url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/congo.mp4";
//        url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/yangli.mp4";
    }

    private void playVideo(String fileId){
        String url = FilesManager.DIRECTORY+"/"+ fileId;
        List<VideoFile> list = DatabaseManager.getInstance().getQueryByWhere(VideoFile.class, "fileId",new String[]{fileId});
        if (list != null && list.size() > 0){
            VideoFile file = list.get(0);
//            String key = file.getKey();
//            int length = file.getLength();
//            mMediaPlayerWrapper.openLocalFile(url, key, length);
            mMediaPlayerWrapper.openRemoteFile(url);
            mMediaPlayerWrapper.prepare();
        }
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
    public void start(String fileId) {
        mMediaPlayerWrapper.pause();
        mMediaPlayerWrapper.destroy();
        mMediaPlayerWrapper.init();
        playVideo(fileId);
        mMediaPlayerWrapper.pause();
        Log.e(TAG,"向服务器请求目前的播放进度");
        ServiceManager.getInstance().requestProgress();
    }

    @Override
    public void play(final long position) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                seekTo(position,1);
            }
        });
//        seekTo(position,1);
    }

    @Override
    public void pause(long position) {
        mMediaPlayerWrapper.pause();
    }

    @Override
    public void seekTo(long position, int status) {
        Log.e(TAG, "跳转到："+position+"--------"+"状态："+status);
        mMediaPlayerWrapper.seekTo(position);

        if (status == 1){
            Log.e(TAG, "开始播放");
            mMediaPlayerWrapper.start();
//            mMediaPlayerWrapper.start();
        }else{
            pause(1);
        }

    }

    @Override
    public void stop() {
        int l = mMediaPlayerWrapper.getPlayer().getDuration();
        if (l > 0){
            mMediaPlayerWrapper.seekTo(l);
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void onBackPressed() {
        if (mMediaPlayerWrapper.getPlayer().isPlaying()){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            this.startActivity(intent);
        }else {
            super.onBackPressed();
        }

    }
}
