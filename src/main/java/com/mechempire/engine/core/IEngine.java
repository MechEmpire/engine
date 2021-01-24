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
     */
    void init() throws Exception;

    /**
     * run engine.
     */
    void run() throws Exception;

    /**
     * close engine.
     */
    void close() throws Exception;
}