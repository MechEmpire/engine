package com.mechempire.engine.constant;

/**
 * package: com.mechempire.engine.constant
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/18 上午10:44
 */
public class ServerConstant {

    // listen
    /**
     * server host
     */
    public static final String host = "0.0.0.0";

    /**
     * server port
     */
    public static final int port = 6666;

    // 心跳

    /**
     * 心跳间隔, 单位: 秒
     */
    public static final int HEART_GAP = 1;

    /**
     * 心跳写超时时间, 单位: 秒
     */
    public static final int SESSION_HEART_WRITE_TIMEOUT = HEART_GAP * 60;

    /**
     * 心跳读超时时间, 单位: 秒
     */
    public static final int SESSION_HEART_READ_TIMEOUT = HEART_GAP * 60;

    /**
     * 心跳读写超时时间, 单位: 秒
     */
    public static final int SESSION_HEART_ALL_TIMEOUT = HEART_GAP * 60;
}