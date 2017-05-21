package com.sd.vr.education.network.http.downloader;

/**
 * 错误码
 * Created by hl09287 on 2017/4/14.
 */
public class ErrorCode {

    public static final int CODE_CONNECT_ERROR = -1;

    public static final int CODE_RESPONSE_CODE_ERROR = -2;

    public static final int CODE_LOAD_ERROR = -3;

    public static final int CODE_UNKNOWN_ERROR = -4;

    private int code;

    private Exception cause;

    public ErrorCode(int code, Exception e) {
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
