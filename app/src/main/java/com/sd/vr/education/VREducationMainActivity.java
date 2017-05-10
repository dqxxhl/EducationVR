package com.sd.vr.education;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sd.vr.R;
import com.sd.vr.ctrl.netty.protobuf.MessageProto;
import com.sd.vr.education.broadcastreceiver.PowerConnectionReceiver;
import com.sd.vr.education.entity.FileDownLoad;
import com.sd.vr.education.entity.VideoItem;
import com.sd.vr.education.presenter.FilesManager;
import com.sd.vr.education.presenter.ServiceManager;
import com.sd.vr.education.presenter.ViewAction;
import com.sd.vr.education.utils.Utils;
import com.sd.vr.education.view.VideoGridViewAdapter;
import com.sd.vr.education.vrplayer.VideoPlayerActivity;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VREducationMainActivity extends Activity implements ViewAction, View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = VREducationMainActivity.class.getName();
    private static final int MSG_KEY_1 = 1;
    private static final int MSG_KEY_2 = 2;
    ServiceManager serviceManager;
    String separator = ".";

    //==================测试代码======================================
    Button sendConnectButton;
    Button sendRegisterButton;
    TextView process;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_KEY_1:
                    long time = System.currentTimeMillis();
                    final Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis(time);
                    int hour = mCalendar.get(Calendar.HOUR);
                    int min = mCalendar.get(Calendar.MINUTE);
                    int apm = mCalendar.get(Calendar.AM_PM);
                    String AM = "AM";
                    if (apm == 1){
                        AM = "PM";
                    }
                    String temp = "";
                    if (min <10){
                        temp = "0";
                    }
                    String timeString = hour+":"+temp+min+" "+AM;
                    top_time.setText(timeString);
                    break;
                case MSG_KEY_2:
                    List<InetAddress> ipList = (List<InetAddress>) msg.obj;
                    if (ipList.size() >= 1){
                        String ip = ipList.get(0).toString();
                        ip = ip.substring(1,ip.length());
                        if (ip != null && !ip.equals("")){
                            String[] ipNum = ip.split("\\.");
                            if (ipNum.length == 4){
                                ip_1.setText(ipNum[0]);
                                ip_2.setText(ipNum[1]);
                                ip_3.setText(ipNum[2]);
                                ip_4.setText(ipNum[3]);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    EditText editText;
    Button lianjie;
    int temp = 0;

    //=============================UI实现=================================
    List<List<VideoItem>> pagerList = new ArrayList<>();
    private ViewPager viewPager;
    private LinearLayout numsLayout;
    private int positionSelected;
    private VideoPagerAdapter adapter;
    private ImageView pre;
    private ImageView next;
    private RelativeLayout homeLayout;
    private RelativeLayout settingLayout;
    private RelativeLayout videoLayout;
    private RelativeLayout mainSetting;
    private RelativeLayout mainSettingIP;
    private RelativeLayout showSetting;
    private RelativeLayout setting_ip;
    private RelativeLayout setting_cache;
    private Button save_ip;
    private Button cancel_ip;
    private EditText ip_1;
    private EditText ip_2;
    private EditText ip_3;
    private EditText ip_4;
    private TextView text_ip;
    private RelativeLayout tuichu;
    private RelativeLayout qiehuan;
    private RelativeLayout layout_pre;
    private RelativeLayout layout_next;
    private TextView text_cache;
    private RelativeLayout layout_null;
    private RelativeLayout pager_index;
    private TextView top_time;
    private Button zidongjiance;
    private ImageView icon_wifi;
    private ProgressBar process_dianliang;
    private TextView text_dianliang;
    private PowerConnectionReceiver receiver = new PowerConnectionReceiver();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "VREducationMainActivity:onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏幕显示
        setContentView(R.layout.activity_education_vrmain);
        serviceManager = ServiceManager.getInstance();
        serviceManager.bindAction(this);

        layout_null = (RelativeLayout) findViewById(R.id.layout_null);
        pager_index = (RelativeLayout) findViewById(R.id.pager_index);
        viewPager = (ViewPager) findViewById(R.id.viewpager_test);
        numsLayout = (LinearLayout) findViewById(R.id.num);
        homeLayout = (RelativeLayout) findViewById(R.id.image_shouye);
        settingLayout = (RelativeLayout) findViewById(R.id.image_shezhi);
        videoLayout = (RelativeLayout) findViewById(R.id.video_main);
        mainSetting = (RelativeLayout) findViewById(R.id.main_setting);
        mainSettingIP = (RelativeLayout) findViewById(R.id.main_setting_ip);
        showSetting = (RelativeLayout) findViewById(R.id.show_setting);
        setting_ip = (RelativeLayout) findViewById(R.id.setting_ip);
        setting_cache = (RelativeLayout) findViewById(R.id.setting_cache);
        save_ip = (Button) findViewById(R.id.save_ip);
        cancel_ip = (Button) findViewById(R.id.cancel_ip);
        text_ip = (TextView) findViewById(R.id.text_ip);
        tuichu = (RelativeLayout) findViewById(R.id.tuichu);
        qiehuan = (RelativeLayout) findViewById(R.id.qiehuan);
        ip_1 = (EditText) findViewById(R.id.ip_1);
        ip_2 = (EditText) findViewById(R.id.ip_2);
        ip_3 = (EditText) findViewById(R.id.ip_3);
        ip_4 = (EditText) findViewById(R.id.ip_4);
        layout_pre = (RelativeLayout) findViewById(R.id.layout_pre);
        layout_next = (RelativeLayout) findViewById(R.id.layout_next);
        text_cache = (TextView) findViewById(R.id.text_cache);
        top_time = (TextView) findViewById(R.id.top_time);
        zidongjiance = (Button) findViewById(R.id.zidongjiance);
        icon_wifi = (ImageView) findViewById(R.id.icon_wifi);
        process_dianliang = (ProgressBar) findViewById(R.id.process_dianliang);
        text_dianliang = (TextView) findViewById(R.id.text_dianliang);

        homeLayout.setOnClickListener(this);
        settingLayout.setOnClickListener(this);
        setting_ip.setOnClickListener(this);
        setting_cache.setOnClickListener(this);
        save_ip.setOnClickListener(this);
        cancel_ip.setOnClickListener(this);
        tuichu.setOnClickListener(this);
        qiehuan.setOnClickListener(this);
        layout_pre.setOnClickListener(this);
        layout_next.setOnClickListener(this);
        zidongjiance.setOnClickListener(this);

        initDate();

        adapter = new VideoPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        onPageSelected(0);

        initView();

        new TimeThread().start();


        //=========================================测试用代码=================================
        sendConnectButton = (Button) findViewById(R.id.connect_send);
        sendConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //向服务端发送数据
//                MessageProto.ReConnectRequest reConnectRequest = MessageProto.ReConnectRequest.newBuilder().setEventId("REGISTER").setEquipmentId(Utils.getDeviceId(VREducationMainActivity.this)).build();
//                MessageProto.MessageRequest request = MessageProto.MessageRequest.newBuilder().setType(MessageProto.Types.RECONNECT).setReConnectRequest(reConnectRequest).build();
//                System.out.println("发送数据："+request.toString());
//                serviceManager.sendRequest(request);

//                serviceManager.requestProgress();
                start("yangli.mp4",32349087);
//
//                long i = Utils.stringToLong("1223123");
//                Toast.makeText(VREducationMainActivity.this, i+"", Toast.LENGTH_LONG).show();
            }
        });

        sendRegisterButton = (Button) findViewById(R.id.register_send);
        sendRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //向服务端发送数据
                MessageProto.RegisterRequest registerRequest = MessageProto.RegisterRequest.newBuilder().setEventId("RECONNECT").setEquipmentId(Utils.getDeviceId(VREducationMainActivity.this)).build();
                MessageProto.MessageRequest request = MessageProto.MessageRequest.newBuilder().setType(MessageProto.Types.REGISTER).setRegisterRequest(registerRequest).build();
                System.out.println("发送数据："+request.toString());
                serviceManager.sendRequest(request);
            }
        });

        process = (TextView) findViewById(R.id.process);

        editText = (EditText) findViewById(R.id.ip);
        lianjie = (Button) findViewById(R.id.lianjie);
        lianjie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip = editText.getText().toString();
                if (ip == null || ip.equals("")){
                    Toast.makeText(VREducationMainActivity.this, "ip 格式不正确", Toast.LENGTH_LONG).show();
                }else {
                    ServiceManager.getInstance().initSocketClient(ip);
                }
            }
        });
    }

    public void initView(){
        //更新文件大小
        File fileDir = new File(FilesManager.DIRECTORY);
        long size = Utils.getTotalSizeOfFilesInDir(fileDir);//字节
        long k = size/1024;
        long m = k/1024;
        float g = (float) m/1024;
        float num = (float)Math.round(g*100)/100;
        text_cache.setText(num+"GB");

        //设置ip
        String ip = Utils.readIP(this);
        if (ip == null || ip.equals("")){
            return;
        }
        text_ip.setText(ip);
        ServiceManager.getInstance().tryInit(ip);

        //设置是否有wifi
        int netWorkState = Utils.getNetWorkState(this);
        updateWiFi(netWorkState);
    }

    @Override
    public void updateDianliang(float batteryPct){
        int num = Math.round(batteryPct*100);
        text_dianliang.setText(num+"%");
        process_dianliang.setProgress(num);
    }

    @Override
    public void updateWiFi(int netWorkState){
        boolean isWifi = true;
        if (netWorkState == Utils.NETWORK_NONE){
            isWifi = false;
        }
        if (isWifi){
            icon_wifi.setImageResource(R.drawable.vr_03);
        }else {
            icon_wifi.setImageResource(R.drawable.nowifi);
        }
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume()");
        super.onResume();
        //设置电量
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void updateUI(){
        initDate();
        adapter.notifyDataSetChanged();
        updateNum();
    }

    /**
     * 准备数据源
     */
    private void initDate(){
        pagerList.clear();
        List<VideoItem> videoList = FilesManager.getInstance().getVideoFiles();
        if (videoList == null || videoList.size() == 0){
            //无本地视频
            layout_null.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.INVISIBLE);
            pager_index.setVisibility(View.INVISIBLE);
        }else{
            layout_null.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            pager_index.setVisibility(View.VISIBLE);
            float a =(float) videoList.size()/8;
            int pageNum = (int) Math.ceil(a);
            for (int i = 0; i < pageNum; i++ ){
                List<VideoItem> temp = new ArrayList<>();
                for (int j = i*8; j < (i+1)*8; j++){
                    if (j < videoList.size()){
                        temp.add(videoList.get(j));
                    }
                }
                pagerList.add(temp);
            }
        }

        Log.e(TAG, pagerList.toString());
    }

    @Override
    public void stop() {
        System.out.println("收到服务端指令----->暂停");
    }

    @Override
    public void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void start(String fileId, long size) {

        File file = FilesManager.getInstance().getFile(fileId);
        if (file == null){
            return;
        }
        String url = file.getAbsolutePath();
//        String url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/yangli.mp4";
        Log.e(TAG, "URL:"+url);
        if (checkFileDownLoad(file.getName(),size)){
            Intent intent = new Intent(VREducationMainActivity.this, VideoPlayerActivity.class);
            intent.putExtra("START",url);
            startActivity(intent);
        }else{
            Toast.makeText(this, "该视频未下载", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void updateprocess(final String process) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                VREducationMainActivity.this.process.setText(process);
            }
        });
    }

    @Override
    public void uodateUI() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        });
    }

    private boolean checkFileDownLoad(String fileName, long size){
        File fileDir = new File(FilesManager.DIRECTORY);
        if (fileDir != null && fileDir.listFiles() != null && fileDir.listFiles().length > 0){
            for (File file : fileDir.listFiles()) {
                if (file.getAbsolutePath().endsWith(FilesManager.PATCH_SUFFIX)){
                    if (file.getName().equals(fileName) && Utils.getFileSize(file) == size){
                        return true;
                    }else if (file.getName().equals(fileName) && Utils.getFileSize(file) > size){
                        deleteFile(fileName);
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        positionSelected = position;
        updateNum();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.image_shouye){
            homeLayout.setBackgroundResource(R.drawable.vr_11);
            settingLayout.setBackgroundResource(R.drawable.vr_10_2);
            videoLayout.setVisibility(View.VISIBLE);
            mainSetting.setVisibility(View.GONE);
        }else if (v.getId() == R.id.image_shezhi){
            homeLayout.setBackgroundResource(R.drawable.vr_10_2);
            settingLayout.setBackgroundResource(R.drawable.vr_11);
            videoLayout.setVisibility(View.GONE);
            mainSetting.setVisibility(View.VISIBLE);
        }else if (v.getId() == R.id.setting_ip){
            showSetting.setVisibility(View.GONE);
            mainSettingIP.setVisibility(View.VISIBLE);
            //同步数据
            String ip = text_ip.getText().toString();
            if (ip != null && !ip.equals("")){
                String[] ipNum = ip.split("\\.");
                if (ipNum.length == 4){
                    ip_1.setText(ipNum[0]);
                    ip_2.setText(ipNum[1]);
                    ip_3.setText(ipNum[2]);
                    ip_4.setText(ipNum[3]);
                }
            }
        }else if (v.getId() == R.id.setting_cache){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示"); //设置标题
            builder.setMessage("是否确认清除所有教学资源?");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //清楚缓存
                    File fileDir = new File(FilesManager.DIRECTORY);
                    Utils.deletCache(fileDir);
                    text_cache.setText("0.0GB");
                    updateUI();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }else if(v.getId() == R.id.save_ip){
            //保存ip
            showSetting.setVisibility(View.VISIBLE);
            mainSettingIP.setVisibility(View.GONE);
            //链接网络
            String ip1 = ip_1.getText().toString();
            String ip2 = ip_2.getText().toString();
            String ip3 = ip_3.getText().toString();
            String ip4 = ip_4.getText().toString();

            //简单校验
            if (ip1 == null || ip1.equals("") ||
                    ip2 == null || ip2.equals("") ||
                    ip3 == null || ip3.equals("") ||
                    ip4 == null || ip4.equals("")){
                return;
            }

            String ip = ip1+separator+ip2+separator+ip3+separator+ip4;
//            String ip = "120.26.141.161";
            ServiceManager.getInstance().initSocketClient(ip);

            text_ip.setText(ip);
        }else if (v.getId() == R.id.cancel_ip){
            showSetting.setVisibility(View.VISIBLE);
            mainSettingIP.setVisibility(View.GONE);
        }else if (v.getId() == R.id.tuichu){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            this.startActivity(intent);
        }else if (v.getId() == R.id.qiehuan){
            //不知道怎么弄

        }else if (v.getId() == R.id.layout_pre){
            viewPager.arrowScroll(17);
            Log.e(TAG, "上一页");
        }else if(v.getId() == R.id.layout_next){
            viewPager.arrowScroll(66);
            Log.e(TAG, "下一页");
        }else if (v.getId() == R.id.zidongjiance){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    List<InetAddress> ipList = Utils.searchHost(VREducationMainActivity.this);
                    Message msg = new Message();
                    msg.what = MSG_KEY_2;
                    msg.obj = ipList;
                    handler.sendMessage(msg);
                }
            });
            thread.start();
        }
    }

    /**
     * 更新数字页码
     */
    public void updateNum(){
        numsLayout.removeAllViews();

        int page = adapter.getCount();
        for (int i =0; i< page; i++){
            TextView textView = new TextView(VREducationMainActivity.this);
            textView.setText(i+1+"");
            textView.setTextSize(15);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            if (i == page-1){
                textView.setPadding(0, 0, 0, 0);
            }else{
                textView.setPadding(0, 0, 70, 0);
            }

            if (i == positionSelected){
                textView.setTextColor(Color.parseColor("#1a81f4"));
            }else {
                textView.setTextColor(Color.parseColor("#FFFFFF"));
            }
            numsLayout.addView(textView);
        }
    }


    /**
     * ViewPager适配器
     */
    private class VideoPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pagerList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = LayoutInflater.from(VREducationMainActivity.this).inflate(R.layout.pager_item, container, false);
            GridView gridView = (GridView) view.findViewById(R.id.photo);
            gridView.setAdapter(new VideoGridViewAdapter(pagerList.get(position), VREducationMainActivity.this));
            container.addView(view);
            return view;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    public class TimeThread extends  Thread{
        @Override
        public void run() {
            super.run();
            do{
                try {
                    Message msg = new Message();
                    msg.what = MSG_KEY_1;
                    handler.sendMessage(msg);
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (true);
        }
    }



}
