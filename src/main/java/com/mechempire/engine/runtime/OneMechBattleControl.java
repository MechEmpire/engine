package com.mechempire.engine.runtime;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;
import com.mechempire.engine.core.IBattleControl;
import com.mechempire.sdk.core.game.AbstractGameMapComponent;
import com.mechempire.sdk.core.game.AbstractMech;
import com.mechempire.sdk.core.game.AbstractPosition;
import com.mechempire.sdk.core.game.AbstractVehicle;
import com.mechempire.sdk.math.PositionCal;
import com.mechempire.sdk.runtime.CommandMessage;
import com.mechempire.sdk.runtime.ResultMessage;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
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

    /**
     * 指令处理 => 方法 map
     */
    private final Map<Byte, String> commandHandles = ImmutableMap.<Byte, String>builder()
            .put((byte) 0, "handleMoveToCommand")
            .build();

    /**
     * 世界对象
     */
    @Resource
    private EngineWorld engineWorld;

    @Override
    public void battle(List<CommandMessage> commandMessageList) throws Exception {
        // 一次调用一帧
        for (CommandMessage commandMessage : commandMessageList) {
            byte[] command = commandMessage.getByteSeq();
            CommandMessageReader reader = new CommandMessageReader(command);
            byte commandByte = reader.readByte();
            Method method = getClass().getDeclaredMethod(commandHandles.get(commandByte), CommandMessageReader.class);
            method.invoke(this, reader);
        }

        for (Map.Entry<Integer, AbstractGameMapComponent> entry : engineWorld.getComponents().entrySet()) {
            AbstractGameMapComponent component = entry.getValue();
            AbstractPosition position = component.getPosition();
            resultMessage.appendByteSeq(Bytes.concat(
                    ByteBuffer.allocate(4).putInt(component.getId()).array(),
                    ByteBuffer.allocate(8).putDouble(position.getX()).array(),
                    ByteBuffer.allocate(8).putDouble(position.getY()).array())
            );
        }
//        System.out.println("\n======\n");
//        for (int i = 0; i < resultMessage.getByteSeq().length; i++) {
//            System.out.printf("%02x\t", resultMessage.getByteSeq()[i]);
//        }
        resultMessage.clearByteSeq();
    }

    /**
     * 处理 moveTo 指令
     *
     * @param reader 指令读取器
     */
    private void handleMoveToCommand(CommandMessageReader reader) {
        AbstractVehicle vehicle = (AbstractVehicle) engineWorld.getComponent(reader.readInt());
        if (null != vehicle) {
            AbstractMech mech = vehicle.getMech();
            double toX = reader.readDouble();
            double toY = reader.readDouble();
            double fromX = vehicle.getPosition().getX();
            double fromY = vehicle.getPosition().getY();
            AbstractPosition newPosition = PositionCal.getComponentNextFrame2DPosition(fromX, fromY, toX, toY, mech.getVehicle().getSpeed());
            mech.updatePosition(newPosition);
        }
    }
}