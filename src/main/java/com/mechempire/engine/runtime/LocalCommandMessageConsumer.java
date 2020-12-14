package com.mechempire.engine.runtime;

import com.mechempire.sdk.core.message.AbstractMessage;
import com.mechempire.sdk.core.message.IConsumer;

import java.util.Queue;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 下午7:26
 */
public class LocalCommandMessageConsumer implements IConsumer {

    /**
     * 消息队列
     */
    private Queue<AbstractMessage> queue;

    @Override
    public void setQueue(Queue<AbstractMessage> queue) {
        if (null != this.queue || null == queue) {
            return;
        }
        this.queue = queue;
    }

    @Override
    public AbstractMessage consume() {
        return this.queue.poll();
    }
}