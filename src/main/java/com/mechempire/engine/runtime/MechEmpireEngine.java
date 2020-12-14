package com.mechempire.engine.runtime;

import com.mechempire.engine.core.message.IEngine;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 上午11:24
 * <p>
 * 机甲帝国引擎
 */
public class MechEmpireEngine implements IEngine {

    @Override
    public void init() {

    }

    /**
     * 引擎启动方法
     * <p>
     * 1. load agent.jar
     * 2. gen CommandMessage
     * 3. send to MessageBus
     */
    @Override
    public void run() {
        CommandMessage commandMessage = new CommandMessage();
        CommandProduct commandProduct = new CommandProduct();
        commandProduct.product(commandMessage);
    }
}