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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
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
import com.sd.vr.education.entity.VideoFile;
import com.sd.vr.education.presenter.FilesManager;
import com.sd.vr.education.presenter.ServiceManager;
import com.sd.vr.education.presenter.ViewAction;
import com.sd.vr.education.utils.DatabaseManager;
import com.sd.vr.education.utils.Utils;
import com.sd.vr.education.view.VideoGridViewAdapter;
import com.sd.vr.education.view.VideoGridViewAdapterNew;
import com.sd.vr.education.vrplayer.VideoPlayerActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 首页，展示视频列表，设置等 Created by hl09287 on 2017/4/14.
 */
public class VREducationMainActivity extends Activity
        implements ViewAction, View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = VREducationMainActivity.class.getName();
    private static final int MSG_KEY_1 = 1;
    private static final int MSG_KEY_2 = 2;
    ServiceManager serviceManager;
    String separator = ".";
    // =============================UI实现=================================
    List<List<VideoFile>> pagerList = new ArrayList<>();
    private LinearLayout numsLayout;
    private int positionSelected;
    private VideoPagerAdapter adapter;
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

    private TextView text_ip;
    private RelativeLayout tuichu;
    private RelativeLayout qiehuan;
    private RelativeLayout layout_pre;
    private RelativeLayout layout_next;
    private TextView text_cache;
    private RelativeLayout layout_null;
    private RelativeLayout pager_index;
    private Button zidongjiance;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_KEY_2:
                List<InetAddress> ipList = (List<InetAddress>) msg.obj;
                if (ipList.size() >= 1) {
                    String ip = ipList.get(0).toString();
                    ip = ip.substring(1, ip.length());
                    if (ip != null && !ip.equals("")) {
                        String[] ipNum = ip.split("\\.");
                        if (ipNum.length == 4) {
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
    // ===================二期UI================================
    // 首页四大按钮
    ImageView video_home;
    ImageView video_list;
    ImageView home_setting;
    ImageView jian_jie;
    // 六大页面
    RelativeLayout rl_home_page;
    RelativeLayout rl_videolist_page;
    RelativeLayout rl_setting_page;
    RelativeLayout rl_jianjie_page;
    RelativeLayout rl_xiangqing_page;
    RelativeLayout rl_ip_page;
    // 组件
    ViewPager viewpager_list;
    TextView tv_text_cache;
    TextView tv_text_ip;
    RelativeLayout rl_settingip_layout;// 设置ip
    RelativeLayout rl_settingcache_layout;// 清楚缓存
    Button bt_ipsetting_save;// 保存ip
    Button bt_ipsetting_cencle;// 取消
    Button bt_ip_auto;// 自动检测
    EditText ip_1;
    EditText ip_2;
    EditText ip_3;
    EditText ip_4;
    ImageView iv_xiangqing_tu;
    TextView tv_xiangqing_text;
    TextView tv_xiangqing_title;
    TextView tv_xiangqing_content;
    ImageView iv_item_first;
    TextView tv_item_first;
    ImageView iv_item_second;
    TextView tv_item_second;
    RelativeLayout rl_item_first;
    RelativeLayout rl_item_second;

    // 数据
    List<VideoFile> listForHomepage = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "VREducationMainActivity:onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 全屏幕显示
        setContentView(R.layout.activity_education_vrmain);
        serviceManager = ServiceManager.getInstance();
        serviceManager.bindAction(this);
        layout_null = (RelativeLayout) findViewById(R.id.layout_null);
        pager_index = (RelativeLayout) findViewById(R.id.pager_index);
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

        layout_pre = (RelativeLayout) findViewById(R.id.layout_pre);
        layout_next = (RelativeLayout) findViewById(R.id.layout_next);
        text_cache = (TextView) findViewById(R.id.text_cache);
        zidongjiance = (Button) findViewById(R.id.zidongjiance);
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

        // 二期
        video_home = (ImageView) findViewById(R.id.video_home);
        video_list = (ImageView) findViewById(R.id.video_list);
        home_setting = (ImageView) findViewById(R.id.home_setting);
        jian_jie = (ImageView) findViewById(R.id.jian_jie);

        rl_home_page = (RelativeLayout) findViewById(R.id.rl_home_page);
        rl_videolist_page = (RelativeLayout) findViewById(R.id.rl_videolist_page);
        rl_setting_page = (RelativeLayout) findViewById(R.id.rl_setting_page);
        rl_jianjie_page = (RelativeLayout) findViewById(R.id.rl_jianjie_page);
        rl_xiangqing_page = (RelativeLayout) findViewById(R.id.rl_xiangqing_page);
        rl_ip_page = (RelativeLayout) findViewById(R.id.rl_ip_page);

        viewpager_list = (ViewPager) findViewById(R.id.viewpager_list);
        tv_text_cache = (TextView) findViewById(R.id.tv_text_cache);
        tv_text_ip = (TextView) findViewById(R.id.tv_text_ip);
        rl_settingip_layout = (RelativeLayout) findViewById(R.id.rl_settingip_layout);
        rl_settingcache_layout = (RelativeLayout) findViewById(R.id.rl_settingcache_layout);
        bt_ipsetting_save = (Button) findViewById(R.id.bt_ipsetting_save);
        bt_ipsetting_cencle = (Button) findViewById(R.id.bt_ipsetting_cencle);
        bt_ip_auto = (Button) findViewById(R.id.bt_ip_auto);
        ip_1 = (EditText) findViewById(R.id.et_ip_1);
        ip_2 = (EditText) findViewById(R.id.et_ip_2);
        ip_3 = (EditText) findViewById(R.id.et_ip_3);
        ip_4 = (EditText) findViewById(R.id.et_ip_4);
        tv_text_cache = (TextView) findViewById(R.id.tv_text_cache);
        iv_xiangqing_tu = (ImageView) findViewById(R.id.iv_xiangqing_tu);
        tv_xiangqing_text = (TextView) findViewById(R.id.tv_xiangqing_text);
        tv_xiangqing_title = (TextView) findViewById(R.id.tv_xiangqing_title);
        tv_xiangqing_content = (TextView) findViewById(R.id.tv_xiangqing_title);
        iv_item_first = (ImageView) findViewById(R.id.iv_item_first);
        tv_item_first = (TextView) findViewById(R.id.tv_item_first);
        iv_item_second = (ImageView) findViewById(R.id.iv_item_second);
        tv_item_second = (TextView) findViewById(R.id.tv_item_second);
        rl_item_first = (RelativeLayout) findViewById(R.id.rl_item_first);
        rl_item_second = (RelativeLayout) findViewById(R.id.rl_item_second);

        video_home.setOnClickListener(this);
        video_list.setOnClickListener(this);
        home_setting.setOnClickListener(this);
        jian_jie.setOnClickListener(this);
        rl_settingip_layout.setOnClickListener(this);
        rl_settingcache_layout.setOnClickListener(this);
        bt_ipsetting_save.setOnClickListener(this);
        bt_ipsetting_cencle.setOnClickListener(this);
        bt_ip_auto.setOnClickListener(this);

        initDate();
        adapter = new VideoPagerAdapter();
        viewpager_list.setAdapter(adapter);
        viewpager_list.addOnPageChangeListener(this);
        onPageSelected(0);
        initView();
    }

    public void initView() {
        // 更新文件大小
        File fileDir = new File(FilesManager.DIRECTORY);
        long size = Utils.getTotalSizeOfFilesInDir(fileDir);// 字节
        long k = size / 1024;
        long m = k / 1024;
        float g = (float) m / 1024;
        float num = (float) Math.round(g * 100) / 100;
        tv_text_cache.setText(num + "GB");

        // 设置ip
        String ip = Utils.readIP(this);
        if (ip == null || ip.equals("")) {
            return;
        }
        tv_text_ip.setText(ip);
        ServiceManager.getInstance().tryInit(ip);
        updateHomePage();
    }

    private void updateHomePage() {
        rl_item_first.setVisibility(View.GONE);
        rl_item_second.setVisibility(View.GONE);
        // 首页设置
        if (listForHomepage.size() > 0) {
            rl_item_first.setVisibility(View.VISIBLE);
            final VideoFile file = listForHomepage.get(0);
            String picUrl = file.getImageUrl();
            if (picUrl != null && !"".equals(picUrl)) {
                Picasso.with(this).load(file.getImageUrl()).into(iv_item_first);
            }
            String titleTemp = file.getFileName();
            String title2Name = "《" + titleTemp + "》";
            tv_item_first.setText(title2Name);
            rl_item_first.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openVideo(file);
                }
            });
        }
        if (listForHomepage.size() > 1) {
            rl_item_second.setVisibility(View.VISIBLE);
            final VideoFile file = listForHomepage.get(1);
            String picUrl = file.getImageUrl();
            if (picUrl != null && !"".equals(picUrl)) {
                Picasso.with(this).load(file.getImageUrl()).into(iv_item_second);
            }
            String titleTemp = file.getFileName();
            String title2Name = "《" + titleTemp + "》";
            tv_item_second.setText(title2Name);
            rl_item_second.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openVideo(file);
                }
            });
        }
    }

    @Override
    public void updateDianliang(float batteryPct) {
    }

    @Override
    public void updateWiFi(int netWorkState) {
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void updateUI() {
        initDate();
        adapter.notifyDataSetChanged();
        updateNum();
        updateHomePage();
    }

    /**
     * 准备数据源
     */
    private void initDate() {
        pagerList.clear();
        List<VideoFile> videoList = FilesManager.getInstance().getVideoFiles();
        if (videoList == null || videoList.size() == 0) {
            // 无本地视频

        } else {
            viewpager_list.setVisibility(View.VISIBLE);
            float a = (float) videoList.size() / 4;
            int pageNum = (int) Math.ceil(a);
            for (int i = 0; i < pageNum; i++) {
                List<VideoFile> temp = new ArrayList<>();
                for (int j = i * 4; j < (i + 1) * 4; j++) {
                    if (j < videoList.size()) {
                        temp.add(videoList.get(j));
                    }
                }
                pagerList.add(temp);
            }
        }

        // 准备首页数据源
        int temp = 0;
        for (int i = videoList.size() - 1; i >= 0; i--) {
            if (videoList.get(i).getFileStatus() == FilesManager.STATUS_COMPLETE_DOWNLOAD) {
                listForHomepage.add(videoList.get(i));
                temp++;
            }
            if (temp >= 2) {
                break;
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
        if (file == null) {
            return;
        }
        String url = file.getAbsolutePath();
        // String url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/yangli.mp4";
        Log.e(TAG, "URL:" + url);
        if (checkFileDownLoad(file.getName(), size)) {
            Intent intent = new Intent(VREducationMainActivity.this, VideoPlayerActivity.class);
            intent.putExtra("START", url);
            startActivity(intent);
        } else {
            Toast.makeText(this, "该视频未下载", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void updateprocess(final String process) {
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

    private boolean checkFileDownLoad(String fileName, long size) {
        File fileDir = new File(FilesManager.DIRECTORY);
        if (fileDir != null && fileDir.listFiles() != null && fileDir.listFiles().length > 0) {
            for (File file : fileDir.listFiles()) {
                if (file.getAbsolutePath().endsWith(FilesManager.PATCH_SUFFIX)) {
                    if (file.getName().equals(fileName) && Utils.getFileSize(file) == size) {
                        return true;
                    } else if (file.getName().equals(fileName) && Utils.getFileSize(file) > size) {
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
        /*
         * if (v.getId() == R.id.image_shouye){ homeLayout.setBackgroundResource(R.drawable.vr_11);
         * settingLayout.setBackgroundResource(R.drawable.vr_10_2);
         * videoLayout.setVisibility(View.VISIBLE); mainSetting.setVisibility(View.GONE); }else if
         * (v.getId() == R.id.image_shezhi){ homeLayout.setBackgroundResource(R.drawable.vr_10_2);
         * settingLayout.setBackgroundResource(R.drawable.vr_11);
         * videoLayout.setVisibility(View.GONE); mainSetting.setVisibility(View.VISIBLE); }else if
         * (v.getId() == R.id.setting_ip){ showSetting.setVisibility(View.GONE);
         * mainSettingIP.setVisibility(View.VISIBLE); //同步数据 String ip =
         * text_ip.getText().toString(); if (ip != null && !ip.equals("")){ String[] ipNum =
         * ip.split("\\."); if (ipNum.length == 4){ ip_1.setText(ipNum[0]); ip_2.setText(ipNum[1]);
         * ip_3.setText(ipNum[2]); ip_4.setText(ipNum[3]); } } }else if (v.getId() ==
         * R.id.setting_cache){ AlertDialog.Builder builder = new AlertDialog.Builder(this);
         * builder.setTitle("提示"); //设置标题 builder.setMessage("是否确认清除所有教学资源?");
         * builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
         * @Override public void onClick(DialogInterface dialog, int which) { dialog.dismiss();
         * //清楚缓存 File fileDir = new File(FilesManager.DIRECTORY); Utils.deletCache(fileDir);
         * text_cache.setText("0.0GB"); updateUI(); } }); builder.setNegativeButton("取消", new
         * DialogInterface.OnClickListener() { //设置取消按钮
         * @Override public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
         * }); builder.create().show(); }else if(v.getId() == R.id.save_ip){ //保存ip
         * showSetting.setVisibility(View.VISIBLE); mainSettingIP.setVisibility(View.GONE); //链接网络
         * String ip1 = ip_1.getText().toString(); String ip2 = ip_2.getText().toString(); String
         * ip3 = ip_3.getText().toString(); String ip4 = ip_4.getText().toString(); //简单校验 if (ip1
         * == null || ip1.equals("") || ip2 == null || ip2.equals("") || ip3 == null ||
         * ip3.equals("") || ip4 == null || ip4.equals("")){ return; } // String ip =
         * ip1+separator+ip2+separator+ip3+separator+ip4; String ip = "120.26.141.161";
         * ServiceManager.getInstance().initSocketClient(ip); text_ip.setText(ip); }else if
         * (v.getId() == R.id.cancel_ip){ showSetting.setVisibility(View.VISIBLE);
         * mainSettingIP.setVisibility(View.GONE); }else if (v.getId() == R.id.tuichu){
         */
        /*
         * Intent intent = new Intent(Intent.ACTION_MAIN); intent.addCategory(Intent.CATEGORY_HOME);
         * intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); this.startActivity(intent);
         *//*
           * new Thread () { public void run () { try { Instrumentation inst= new Instrumentation();
           * inst.sendKeyDownUpSync(KeyEvent. KEYCODE_BACK); } catch(Exception e) {
           * e.printStackTrace(); } } }.start(); }else if (v.getId() == R.id.qiehuan){ //不知道怎么弄
           * }else if (v.getId() == R.id.layout_pre){ viewpager_list.arrowScroll(17); Log.e(TAG,
           * "上一页"); }else if(v.getId() == R.id.layout_next){ viewpager_list.arrowScroll(66);
           * Log.e(TAG, "下一页"); }else if (v.getId() == R.id.zidongjiance){ Thread thread = new
           * Thread(new Runnable() {
           * @Override public void run() { List<InetAddress> ipList =
           * Utils.searchHost(VREducationMainActivity.this); Message msg = new Message(); msg.what =
           * MSG_KEY_2; msg.obj = ipList; handler.sendMessage(msg); } }); thread.start(); }
           */

        switch (v.getId()) {
        case R.id.video_home:
            rl_home_page.setVisibility(View.VISIBLE);
            rl_videolist_page.setVisibility(View.GONE);
            rl_setting_page.setVisibility(View.GONE);
            rl_jianjie_page.setVisibility(View.GONE);
            rl_xiangqing_page.setVisibility(View.GONE);
            rl_ip_page.setVisibility(View.GONE);
            break;
        case R.id.video_list:
            rl_home_page.setVisibility(View.GONE);
            rl_videolist_page.setVisibility(View.VISIBLE);
            rl_setting_page.setVisibility(View.GONE);
            rl_jianjie_page.setVisibility(View.GONE);
            rl_xiangqing_page.setVisibility(View.GONE);
            rl_ip_page.setVisibility(View.GONE);
            break;
        case R.id.home_setting:
            rl_home_page.setVisibility(View.GONE);
            rl_videolist_page.setVisibility(View.GONE);
            rl_setting_page.setVisibility(View.VISIBLE);
            rl_jianjie_page.setVisibility(View.GONE);
            rl_xiangqing_page.setVisibility(View.GONE);
            rl_ip_page.setVisibility(View.GONE);
            break;
        case R.id.jian_jie:
            rl_home_page.setVisibility(View.GONE);
            rl_videolist_page.setVisibility(View.GONE);
            rl_setting_page.setVisibility(View.GONE);
            rl_jianjie_page.setVisibility(View.VISIBLE);
            rl_xiangqing_page.setVisibility(View.GONE);
            rl_ip_page.setVisibility(View.GONE);
            break;
        case R.id.rl_settingip_layout:
            rl_home_page.setVisibility(View.GONE);
            rl_videolist_page.setVisibility(View.GONE);
            rl_setting_page.setVisibility(View.GONE);
            rl_jianjie_page.setVisibility(View.GONE);
            rl_xiangqing_page.setVisibility(View.GONE);
            rl_ip_page.setVisibility(View.VISIBLE);
            // 同步数据
            String ip = tv_text_ip.getText().toString();
            if (ip != null && !ip.equals("")) {
                String[] ipNum = ip.split("\\.");
                if (ipNum.length == 4) {
                    ip_1.setText(ipNum[0]);
                    ip_2.setText(ipNum[1]);
                    ip_3.setText(ipNum[2]);
                    ip_4.setText(ipNum[3]);
                }
            }
            break;
        case R.id.rl_settingcache_layout:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示"); // 设置标题
            builder.setMessage("是否确认清除所有教学资源?");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { // 设置确定按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // 清楚缓存
                    File fileDir = new File(FilesManager.DIRECTORY);
                    DatabaseManager.getInstance().delete(VideoFile.class);// 清除数据库
                    Utils.deletCache(fileDir);// 清空文件
                    tv_text_cache.setText("0.0GB");
                    updateUI();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { // 设置取消按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            break;
        case R.id.bt_ipsetting_save:
            // 保存ip
            rl_setting_page.setVisibility(View.VISIBLE);
            rl_ip_page.setVisibility(View.GONE);
            // 链接网络
            String ip1 = ip_1.getText().toString();
            String ip2 = ip_2.getText().toString();
            String ip3 = ip_3.getText().toString();
            String ip4 = ip_4.getText().toString();

            // 简单校验
            /*
             * if (ip1 == null || ip1.equals("") || ip2 == null || ip2.equals("") || ip3 == null ||
             * ip3.equals("") || ip4 == null || ip4.equals("")){ return; }
             */

            // String ipNew = ip1+separator+ip2+separator+ip3+separator+ip4;
            String ipNew = "120.26.141.161";
            ServiceManager.getInstance().initSocketClient(ipNew);
            tv_text_ip.setText(ipNew);
            break;
        case R.id.bt_ipsetting_cencle:
            rl_setting_page.setVisibility(View.VISIBLE);
            rl_ip_page.setVisibility(View.GONE);
            break;
        case R.id.bt_ip_auto:
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
            break;
        }
    }

    /**
     * 进入
     * 
     * @param file
     */
    public void openVideo(final VideoFile file) {
        rl_home_page.setVisibility(View.GONE);
        rl_videolist_page.setVisibility(View.GONE);
        rl_setting_page.setVisibility(View.GONE);
        rl_jianjie_page.setVisibility(View.GONE);
        rl_xiangqing_page.setVisibility(View.VISIBLE);
        rl_ip_page.setVisibility(View.GONE);
        String picUrl = file.getImageUrl();
        if (picUrl != null && !"".equals(picUrl)) {
            Picasso.with(this).load(picUrl).into(iv_xiangqing_tu);
        }
        String titleTemp = file.getFileName();
        String title2Name = "《" + titleTemp + "》";
        tv_xiangqing_text.setText(title2Name);
        tv_xiangqing_title.setText(title2Name);
        tv_xiangqing_content.setText(file.getFileContent());

        iv_xiangqing_tu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VREducationMainActivity.this, VideoPlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("videoFile", file.getFileId());
                intent.putExtras(bundle);
                VREducationMainActivity.this.startActivity(intent);
            }
        });

    }

    /**
     * 更新数字页码
     */
    public void updateNum() {
        numsLayout.removeAllViews();

        int page = adapter.getCount();
        for (int i = 0; i < page; i++) {
            TextView textView = new TextView(VREducationMainActivity.this);
            textView.setText(i + 1 + "");
            textView.setTextSize(15);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            if (i == page - 1) {
                textView.setPadding(0, 0, 0, 0);
            } else {
                textView.setPadding(0, 0, 70, 0);
            }

            if (i == positionSelected) {
                textView.setTextColor(Color.parseColor("#1a81f4"));
            } else {
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

            View view = LayoutInflater.from(VREducationMainActivity.this)
                    .inflate(R.layout.pager_item, container, false);
            GridView gridView = (GridView) view.findViewById(R.id.photo);
            gridView.setAdapter(new VideoGridViewAdapterNew(pagerList.get(position), VREducationMainActivity.this));
            container.addView(view);
            return view;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

}
