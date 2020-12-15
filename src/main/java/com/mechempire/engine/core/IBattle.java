package com.mechempire.engine.core;

import com.mechempire.sdk.runtime.CommandMessage;
import com.mechempire.sdk.runtime.ResultMessage;

/**
 * package: com.mechempire.engine.core
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/15 下午9:54
 */
public interface IBattle {

    /**
     * 对战接口
     *
     * @param commandMessage 指令帧
     * @return 结果帧
     */
    ResultMessage battle(CommandMessage commandMessage);
}