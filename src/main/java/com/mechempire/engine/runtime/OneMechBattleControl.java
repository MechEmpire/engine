package com.mechempire.engine.runtime;

import com.mechempire.engine.core.IBattleControl;
import com.mechempire.sdk.runtime.CommandMessage;

import java.util.Arrays;
import java.util.List;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/15 下午9:57
 * <p>
 * 只有一个机甲的对战逻辑
 */
public class OneMechBattleControl implements IBattleControl {

    @Override
    public void battle(List<CommandMessage> commandMessageList) {
        for (CommandMessage commandMessage : commandMessageList) {
            System.out.printf("%s, team_id: %d \n", Thread.currentThread().getName(), commandMessage.getTeamId());
            byte[] command = commandMessage.getCommandSeq();
            CommandMessageReader reader = new CommandMessageReader(command);
            System.out.printf("command: %x, object_id: %d, x:%.2f, y:%.2f\n", reader.readByte(), reader.readInt(), reader.readDouble(), reader.readDouble());
            System.out.println(Arrays.toString(command));
        }
    }
}