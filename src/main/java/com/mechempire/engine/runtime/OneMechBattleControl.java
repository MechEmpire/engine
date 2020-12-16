package com.mechempire.engine.runtime;

import com.google.common.collect.ImmutableMap;
import com.mechempire.engine.core.IBattleControl;
import com.mechempire.sdk.core.game.AbstractMech;
import com.mechempire.sdk.core.game.AbstractVehicle;
import com.mechempire.sdk.runtime.CommandMessage;
import com.mechempire.sdk.runtime.Position2D;
import com.mechempire.sdk.runtime.ResultMessage;
import com.mechempire.sdk.util.ClassCastUtil;

import javax.annotation.Resource;
import java.lang.reflect.Method;
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
        for (CommandMessage commandMessage : commandMessageList) {
            byte[] command = commandMessage.getCommandSeq();
            CommandMessageReader reader = new CommandMessageReader(command);
            byte commandByte = reader.readByte();
            Method method = getClass().getDeclaredMethod(commandHandles.get(commandByte), CommandMessageReader.class);
            method.invoke(this, reader);
        }
    }

    private void handleMoveToCommand(CommandMessageReader reader) {
        AbstractVehicle vehicle = (AbstractVehicle) engineWorld.getComponent(reader.readInt());
        if (null != vehicle) {
            AbstractMech mech = vehicle.getMech();
            System.out.printf("startX: %.2f, startY: %.2f\n", mech.getStartX(), mech.getStartY());
            Position2D position = ClassCastUtil.cast(vehicle.getPosition());

//            System.out.printf("positionX: %.2f, positionY: %.2f\n", position.getX(), position.getY());

//            System.out.println((mech.getVehicleClazz().cast(mech.getVehicle())).getClass().getName());

//            mech.getVehicleClazz().cast(mech.getVehicle());
//            System.out.println(vehicle.getMech().getVehicleClazz());
//            System.out.printf("speed: %.2f\n", vehicle.getMech().getVehicle().getSpeed());
            System.out.printf("speed: %.2f\n", vehicle.getSpeed());
        }
    }
}