package com.mechempire.engine.core.message;

/**
 * package: com.mechempire.engine.core.game
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 上午9:01
 * <p>
 * 消息生产者接口
 */
public interface IProducer {
    /**
     * 生产消息
     *
     * @return 消息对象
     */
    void product(AbstractMessage message);
}