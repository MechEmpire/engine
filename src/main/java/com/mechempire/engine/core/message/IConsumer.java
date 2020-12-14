package com.mechempire.engine.core.message;

/**
 * package: com.mechempire.engine.core.message
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 上午9:04
 * <p>
 * 消息消费者接口
 */
public interface IConsumer {

    /**
     * 消费消息
     */
    void consume(AbstractMessage message);
}