package com.mechempire.engine.factory;

/**
 * package: com.mechempire.sdk.core.factory
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 下午2:38
 */
public class MechFactory {
    /**
     * 创建并装配机甲
     *
     * @param mechClazz 机甲类
     * @param <M>       类泛型
     * @return 机甲对象
     * @throws Exception 异常
     */
//    public static <M extends AbstractMech> M newMech(Class<M> mechClazz) throws Exception {
//        M mech = GameMapComponentFactory.getComponent(mechClazz);
//        mech.setStartY(64.0);
//        mech.setStartX(64.0);
//
//        // 装配载具, 设置所属机甲,大小
//        AbstractVehicle vehicle = GameMapComponentFactory.getComponent(ClassCastUtil.cast(mech.getVehicleClazz()));
//        vehicle.setStartY(mech.getStartX());
//        vehicle.setStartY(mech.getStartY());
//        vehicle.setMech(mech);
//        mech.setWidth(vehicle.getWidth());
//        mech.setLength(vehicle.getLength());
//        mech.setVehicle(vehicle);
//
//        // 装配武器, 设置所属机甲
//        AbstractWeapon weapon = GameMapComponentFactory.getComponent(ClassCastUtil.cast(mech.getWeaponClazz()));
//        weapon.setMech(mech);
//        mech.setWeapon(weapon);
//
//        // 装配弹药, 设置所属机甲
//        AbstractAmmunition ammunition = GameMapComponentFactory.getComponent(ClassCastUtil.cast(mech.getAmmunitionClazz()));
//        ammunition.setMech(mech);
//        mech.setAmmunition(ammunition);
//
//        // 更新初始位置信息
//        AbstractPosition position = PositionFactory.getPosition(mech);
//        mech.updatePosition(position);
//        return mech;
//    }
}