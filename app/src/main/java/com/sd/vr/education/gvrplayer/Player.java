package com.sd.vr.education.gvrplayer;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.sd.vr.R;
import com.sd.vr.education.presenter.FilesManager;
import com.sd.vr.education.presenter.ServiceManager;
import com.sd.vr.education.presenter.VideoAction;


public class Player extends GvrActivity implements SensorEventListener, VideoAction {

    private GvrView view;
//    private String url=Environment.getExternalStorageDirectory()+"/f.jpg";
//    private String url=Environment.getExternalStorageDirectory()+"/v.mp4";
    private String url;
    private SceneRenderer renderer;

    private SensorManager mSensorManager;
    private Sensor mRotationSensor;

    private float[] position = new float[2];
    private float distance = -1;
    private boolean moviendo;

    private int hola;

    //业务相关
    private Handler handler = new Handler();
    private String fileId;

    //测试代码
    Button bt_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);

        Intent intent = getIntent();
        fileId =  intent.getStringExtra("videoFile");
        url = FilesManager.DIRECTORY+"/"+ fileId;

        view = (GvrView) findViewById(R.id.gvrView);
        renderer = new SceneRenderer(this, url);
        view.setRenderer(renderer);
//        renderer.trigger();
        view.setStereoModeEnabled(true);
        view.setDistortionCorrectionEnabled(true);

        mSensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mRotationSensor, 100000);

        view.resetHeadTracker();

        ServiceManager.getInstance().bindVideoAction(this);


        //test
        bt_start = (Button) findViewById(R.id.bt_start);
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderer.start();
            }
        });

    }

    @Override
    protected void onPause() {
        renderer.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        renderer.releasePlayer();
        view.shutdown();
        super.onDestroy();
        ServiceManager.getInstance().unBindVideoAction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderer.restart();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mRotationSensor) {
            if (event.values.length > 4) {
                float[] truncatedRotationVector = new float[4];
                System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                update(truncatedRotationVector);
            } else {
                update(event.values);
            }
        }
    }

    private void update(float[] vectors) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
        int worldAxisX = SensorManager.AXIS_X;
        int worldAxisZ = SensorManager.AXIS_Z;
        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);
        float pitch = orientation[1] * -57;
        float roll = orientation[2] * -57;
        if (roll > 45) {
            view.setStereoModeEnabled(true);
        } else {
            view.setStereoModeEnabled(false);
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        girar(event);
        return super.onTouchEvent(event);
    }

    private void girar(MotionEvent event){

        int count = event.getPointerCount();
        hola++;

        if (count == 1 && !view.getStereoModeEnabled() && distance==-1) {

            MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
            event.getPointerCoords(0, coords);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                view.setNeckModelEnabled(false);

                position[0] = coords.x;
                position[1] = coords.y;
                renderer.rotation[0] = 0;
                renderer.rotation[1] = 0;

                moviendo = true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE && moviendo) {

                float[] newPosition = new float[2];
                newPosition[0] = coords.x;
                newPosition[1] = coords.y;

                renderer.rotation[0] = (position[0] - newPosition[0]) / 10;
                renderer.rotation[1] = (newPosition[1] - position[1]) / 10;

                position = newPosition;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                position[0] = -1;
                position[1] = -1;
                moviendo=false;

            }

            float x = coords.x;
            float y = coords.y;
//            Log.d("coord", "" + x + "," + y);
        } else {
            if (count == 2 && !view.getStereoModeEnabled()) {
                moviendo=false;
                MotionEvent.PointerCoords coords1 = new MotionEvent.PointerCoords();
                MotionEvent.PointerCoords coords2 = new MotionEvent.PointerCoords();
                event.getPointerCoords(0, coords1);
                event.getPointerCoords(1, coords2);

                if(distance == -1){
                    float x_d = coords1.x - coords2.x;
                    float y_d = coords1.y - coords2.y;
                    distance = (float) Math.sqrt(x_d * x_d + y_d * y_d);
                }

                if (event.getAction()==MotionEvent.ACTION_MOVE) {
                    float x_d = coords1.x - coords2.x;
                    float y_d = coords1.y - coords2.y;
                    float d = (float) Math.sqrt(x_d * x_d + y_d * y_d);
                    renderer.changeFov((distance - d)/20.0f);
                    distance = d;
                } else{
                    distance = -1;
                }

            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void start(String url, long size) {
        if(url.equals(fileId)){
            ServiceManager.getInstance().requestProgress();
        }else{
            renderer.reset();
            if (FilesManager.getInstance().getFile(url) != null){
                if (FilesManager.checkFileDownLoad(url, size)){
                    fileId = url;
                    renderer.prepareVideo(FilesManager.DIRECTORY+"/"+ url);
                }
            }
        }
    }

    @Override
    public void play(final long position, final String fileId) {
        if (!fileId.equals(this.fileId)){
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                seekTo(position,1,fileId);
            }
        });
    }

    @Override
    public void pause(long position, String fileId) {
        if (!fileId.equals(this.fileId)){
            return;
        }
        renderer.seekTo(position);
    }

    @Override
    public void seekTo(final long position, final int status, final String fileId) {
        if (!fileId.equals(this.fileId)){
            return;
        }
        renderer.seekTo(position);
        if (status == 1){
            renderer.start();
        }else {
            renderer.pause();
        }
    }

    @Override
    public void stop(String fileId) {
        if (!fileId.equals(this.fileId)){
            return;
        }
        int l = renderer.getPlayer().getDuration();
        if (l>=0){
            renderer.seekTo(l);
        }
    }

    @Override
    public void shutdown() {
        renderer.pause();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (renderer.getPlayer().isPlaying() || renderer.getPlayer().getCurrentPosition() > 0){
            ServiceManager.getInstance().finish();
        }else {
            ServiceManager.getInstance().unBindVideoAction();
            renderer.pause();
            finish();
        }
    }



}