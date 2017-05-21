package com.sd.vr.education.network.http.downloader;

import java.net.URLConnection;

/**
 * 工具类
 * Created by hl09287 on 2017/4/14.
 */
public class LoaderUtils {
    public static void setHeader(URLConnection conn) {
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Connection", "Keep-Alive");
    }
}
