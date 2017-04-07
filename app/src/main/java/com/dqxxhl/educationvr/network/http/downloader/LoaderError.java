package com.dqxxhl.educationvr.network.http.downloader;

/**
 * Created by sk on 15-8-5.
 */
public class LoaderError {

    /**
     * 链接失败
     */
    public static final int CODE_CONNECT_ERROR = -1;

    /**
     * 响应码错误 ，非 200 或者 206
     */
    public static final int CODE_RESPONSE_CODE_ERROR = -2;

    /**
     * 下载失败，包含 read time out 等
     */
    public static final int CODE_LOAD_ERROR = -3;

    /**
     * 未知错误
     */
    public static final int CODE_UNKNOWN_ERROR = -4;

    private int code;

    private Exception cause;

    public LoaderError(int code, Exception e) {
        this.code = code;
        this.cause = e;
    }

    public Exception getCause() {
        return this.cause;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.cause.getLocalizedMessage();
    }

}
