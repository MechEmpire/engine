package com.mechempire.engine.runtime;

import com.mechempire.engine.core.IBattle;
import com.mechempire.sdk.runtime.CommandMessage;
import com.mechempire.sdk.runtime.ResultMessage;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/15 下午9:57
 * <p>
 * 只有一个机甲的对战逻辑
 */
public class OneMechBattle implements IBattle {

    @Override
    public ResultMessage battle(CommandMessage commandMessage) {
        return null;
    }
}