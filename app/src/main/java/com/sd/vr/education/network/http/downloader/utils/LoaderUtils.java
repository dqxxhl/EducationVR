package com.sd.vr.education.network.http.downloader.utils;

import java.net.URLConnection;

/**
 * Created by sk on 15-10-10.
 */
public class LoaderUtils {
    public static void setHeader(URLConnection conn) {
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Connection", "Keep-Alive");
    }
}
