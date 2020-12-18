package com.mechempire.engine.network.session;

/**
 * package: com.mechempire.engine.server.session
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/18 上午8:50
 */
public interface ISession {

    /**
     * 判断当前会话是否处于连接状态
     *
     * @return 是否连接
     */
    boolean isConnected();

    /**
     * 关闭会话
     *
     * @param immediately 是否立即关闭
     */
    void close(boolean immediately);

    /**
     * 向会话发送字节消息
     *
     * @param message 消息数组
     * @throws Exception 异常
     */
    void write(byte[] message) throws Exception;
}