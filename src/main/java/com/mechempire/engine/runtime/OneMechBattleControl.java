package com.mechempire.engine.runtime;

import com.google.common.collect.ImmutableMap;
import com.mechempire.engine.core.IBattleControl;
import com.mechempire.sdk.core.game.AbstractGameMapComponent;
import com.mechempire.sdk.runtime.CommandMessage;
import com.mechempire.sdk.runtime.ResultMessage;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/15 下午9:57
 * <p>
 * 只有一个机甲的对战逻辑
 */
public class OneMechBattleControl implements IBattleControl {

    /**
     * 结果帧
     */
    private final ResultMessage resultMessage = new ResultMessage();

    private Map<Byte, String> commandHandles = ImmutableMap.<Byte, String>builder()
            .put((byte) 0, "handleMoveToCommand")
            .build();

//    private Map<Byte, List<Class<?>>> commandHandleParams = ImmutableMap.<Byte, List<Class<?>>>builder()
//            .put((byte) 0, new ArrayList<Class<?>> {{}})
//            .build();

    /**
     * 世界对象
     */
    @Resource
    private EngineWorld engineWorld;

    @Override
    public void battle(List<CommandMessage> commandMessageList) throws Exception {
        for (CommandMessage commandMessage : commandMessageList) {
            byte[] command = commandMessage.getCommandSeq();
            CommandMessageReader reader = new CommandMessageReader(command);
            byte commandByte = reader.readByte();
//            Method method = getClass().getDeclaredMethod(commandHandles.get(commandByte), int.class, double.class, double.class);
//            method.invoke(this, reader.readInt(), reader.readDouble(), reader.readDouble());
            handleMoveToCommand(reader.readInt(), reader.readDouble(), reader.readDouble());
        }
    }

    private void handleMoveToCommand(int componentId, double x, double y) {
        AbstractGameMapComponent component = engineWorld.getComponent(componentId);
        if (null != component) {
            System.out.printf("hh %d\n", component.getId());
        }
    }
}