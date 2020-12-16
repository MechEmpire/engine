package com.mechempire.engine.core;

import com.mechempire.sdk.runtime.CommandMessage;

import java.util.List;

/**
 * package: com.mechempire.engine.core
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/15 下午9:54
 */
public interface IBattleControl {

    /**
     * 对战接口
     *
     * @param commandMessageList 指令帧列表
     */
    void battle(List<CommandMessage> commandMessageList);
}