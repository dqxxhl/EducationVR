package com.sd.vr.education.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * 工具类
 * Created by hl09287 on 2017/3/29.
 */

public class Utils {

    private static final String TAG = Utils.class.getName();
    private static final String SAVE_IP = "save_ip";
    private static final String IP = "ip";

    public static String getDeviceId(Context context) {
        String deviceId = "";
        if (deviceId == null || "".equals(deviceId)) {
            try {
                deviceId = getAndroidId(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (deviceId == null || "".equals(deviceId)) {
            if (deviceId == null || "".equals(deviceId)) {
                UUID uuid = UUID.randomUUID();
                deviceId = uuid.toString().replace("-", "");
            }
        }
        return deviceId;
    }


    // IMEI码
    private static String getIMIEStatus(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        return deviceId;
    }

    // Mac地址
    private static String getLocalMac(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    // Android Id
    private static String getAndroidId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

    public static long getFileSize(File file)
    {
        long size = 0;
        if (file.exists()){
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Log.e("获取文件大小","文件不存在!");
        }
        return size;
    }


    /**
     * 没有连接网络
     */
    public static final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    public static final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    public static final int NETWORK_WIFI = 1;

    public static int getNetWorkState(Context context) {
        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {

            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }


    private static InetAddress getIP(byte i, Context context){
        //传进来的i是用来做最后一个段的
        InetAddress local = null;
        try {
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();

            local = InetAddress.getLocalHost();//获得本地ip
            byte[]addr=local.getAddress(); //将ip转换为字节数组存入addr
            addr[0] = (byte)(ipAddress & 0xff);
            addr[1] = (byte)(ipAddress>>8 & 0xff);
            addr[2] = (byte)(ipAddress>>16 & 0xff);
            addr[3] = i;//修改ip的最后一个段
            local = InetAddress.getByAddress(addr); //将数组转换成IP
        } catch (Exception e){
            e.printStackTrace();
        }
        return local;
    }

    /**
     * 寻找主机
     */
    public static List<InetAddress> searchHost(Context context){
        List<InetAddress> ipList = new ArrayList<>();
        int PORT=8011;//定义端口
        for(int i=1;i<=254;i++){
            try {
                Socket s=new Socket();
                s.connect(new InetSocketAddress(getIP((byte)i,context),PORT), 100);//试着连接每一个ip 每个ip延迟时间为100毫秒
                Log.e(TAG, "连接成功了:"+getIP((byte)i,context));
                ipList.add(getIP((byte)i,context));
                break;
            } catch (IOException e) {
                Log.e(TAG, "第 " +i+" 个IP "+(byte)i+"连接尝试失败");
                continue;
            }
        }

        return ipList;
    }

    /**
     * 字符串转化为Long类型
     * @param str
     * @return
     */
    public static long stringToLong(String str){
        if (str == null || str.equals("")){
            return -1;
        }
        int index = str.indexOf(".");
        if (index != -1){
            String temp = str.substring(0,index);
            return Long.valueOf(temp);
        }else{
            return Long.valueOf(str);
        }
    }

    // 递归方式 计算文件的大小
    public static long getTotalSizeOfFilesInDir(final File file) {
        if (file.isFile()){
            return file.length();

        }
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null){
            for (final File child : children){
                total += getTotalSizeOfFilesInDir(child);
            }
        }
        return total;
    }

    /**
     * 清空文件夹
     * @param file
     */
    public static void deletCache(File file){
        if(file ==null){
            return;
        }
        final File[] children = file.listFiles();
        if (children != null){
            for (File child : children){
                child.delete();
            }
        }
    }

    /**
     * 保存ip
     * @param context
     * @param ip
     */
    public static void saveIP(Context context, String ip){
        SharedPreferences sp = context.getSharedPreferences(SAVE_IP, Context.MODE_PRIVATE);
        sp.edit().putString(IP, ip).commit();
    }

    /**
     * 读取ip
     * @return
     */
    public static String readIP(Context context){
        SharedPreferences sp = context.getSharedPreferences(SAVE_IP, Context.MODE_PRIVATE);
        String ip = sp.getString(IP, null);
        return ip;
    }

    public static String subName(String fileName){
        String temp = "《" + fileName + "》";
        String[] strs = fileName.split("\\.");
        if(strs.length > 0){
            temp = "《" + strs[0] + "》";
        }
        return temp;
    }

}
