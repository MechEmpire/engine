package com.mechempire.engine.network.session;

/**
 * 链接状态枚举类
 */
public enum TCPNetState {

    // 连接
    CONNECTED,

    // 掉线中
    DISCONNECTING,

    // 掉线
    DISCONNECTED,

    // 销毁
    DESTROY;
}
