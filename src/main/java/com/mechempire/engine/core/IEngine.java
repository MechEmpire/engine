package com.mechempire.engine.core;

/**
 * package: com.mechempire.engine.core.message
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 上午11:23
 */
public interface IEngine {

    /**
     * init engine
     *
     * @throws Exception 异常
     */
    void init() throws Exception;

    /**
     * 重新使用
     *
     * @throws Exception 异常
     */
    void recycle() throws Exception;

    /**
     * run engine.
     *
     * @throws Exception 异常
     */
    void run() throws Exception;

    /**
     * close engine.
     *
     * @throws Exception 异常
     */
    void close() throws Exception;

    /**
     * 引擎是否关闭
     *
     * @return 是否关闭
     */
    boolean isClosed();

    /**
     * 引擎是否空闲
     *
     * @return 是否空闲
     */
    boolean isIdle();

    /**
     * 引擎是否中断
     *
     * @return 是否中断
     */
    boolean isOccupied();

    /**
     * 切换为空闲
     *
     * @return 切换结果
     */
    boolean switchIdle();

    /**
     * 切换为终端
     *
     * @return 切换结果
     */
    boolean switchOccupied();
}