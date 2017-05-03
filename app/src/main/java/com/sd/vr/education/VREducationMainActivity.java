package com.sd.vr.education;


import com.sd.vr.R;
import com.sd.vr.education.presenter.FilesManager;
import com.sd.vr.education.presenter.ServiceManager;
import com.sd.vr.education.presenter.ViewAction;
import com.sd.vr.education.utils.Utils;
import com.sd.vr.ctrl.netty.protobuf.MessageProto;
import com.sd.vr.education.view.VideoGridViewAdapter;
import com.sd.vr.education.vrplayer.VideoPlayerActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VREducationMainActivity extends Activity implements ViewAction, View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = VREducationMainActivity.class.getName();

    ServiceManager serviceManager;
    String separator = ".";

    //==================测试代码======================================
    Button sendConnectButton;
    Button sendRegisterButton;
    TextView process;
    Handler handler = new Handler();
    EditText editText;
    Button lianjie;
    int temp = 0;

    //=============================UI实现=================================
    List<List<String>> pagerList = new ArrayList<>();
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏幕显示
        setContentView(R.layout.activity_education_vrmain);
        serviceManager = ServiceManager.getInstance();
        serviceManager.bindAction(this);

        initDate();

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
        ip_1 = (EditText) findViewById(R.id.ip_1);
        ip_2 = (EditText) findViewById(R.id.ip_2);
        ip_3 = (EditText) findViewById(R.id.ip_3);
        ip_4 = (EditText) findViewById(R.id.ip_4);

        homeLayout.setOnClickListener(this);
        settingLayout.setOnClickListener(this);
        setting_ip.setOnClickListener(this);
        setting_cache.setOnClickListener(this);
        save_ip.setOnClickListener(this);
        cancel_ip.setOnClickListener(this);


        pre = (ImageView) findViewById(R.id.pre);
        next = (ImageView) findViewById(R.id.next);
        pre.setOnClickListener(this);
        next.setOnClickListener(this);

        viewPager = (ViewPager) findViewById(R.id.viewpager_test);
        adapter = new VideoPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        onPageSelected(0);


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
        List<String> videoList = FilesManager.getInstance().getVideoFiles();
        if (videoList == null || videoList.size() == 0){
            //无本地视频
        }else{
            float a =(float) videoList.size()/8;
            Log.e(TAG,"yema  ========="+a);
            int pageNum = (int) Math.ceil(a);
            for (int i = 0; i < pageNum; i++ ){
                List<String> temp = new ArrayList<>();
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
        String url = FilesManager.DIRECTORY+"/"+ fileId;
//        String url = Environment.getExternalStorageDirectory().getAbsolutePath()+"/yangli.mp4";
        Log.e(TAG, "URL:"+url);
        if (checkFileDownLoad(fileId,size)){
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

    private boolean checkFileDownLoad(String fileName, long size){
        File fileDir = new File(FilesManager.DIRECTORY);
        if (fileDir != null && fileDir.listFiles() != null && fileDir.listFiles().length > 0){
            for (File file : fileDir.listFiles()) {
                if (file.getAbsolutePath().endsWith(FilesManager.PATCH_SUFFIX)){
                    if (file.getName().equals(fileName) && Utils.getFileSize(file) == size){
                        //文件已下载
                        ServiceManager.getInstance().sendDownloadAck(fileName);
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
        if (v.getId() == R.id.pre){
            viewPager.arrowScroll(1);
        }else if (v.getId() == R.id.next){
            viewPager.arrowScroll(2);
        }else if (v.getId() == R.id.image_shouye){
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
        }else if (v.getId() == R.id.setting_cache){
            //清楚缓存
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
            ServiceManager.getInstance().initSocketClient(ip);

            text_ip.setText(ip);

        }else if (v.getId() == R.id.cancel_ip){
            showSetting.setVisibility(View.VISIBLE);
            mainSettingIP.setVisibility(View.GONE);
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
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(layoutParams);
            if (i == page){
                textView.setPadding(0, 0, 0, 0);
            }else{
                textView.setPadding(0, 0, 20, 0);
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



}
