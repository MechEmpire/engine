package com.mechempire.engine.runtime;

import com.mechempire.sdk.core.factory.MechFactory;
import com.mechempire.sdk.core.game.AbstractMech;
import com.mechempire.sdk.core.game.AbstractTeam;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/15 下午4:47
 * <p>
 * 队伍工厂类
 */
public class TeamFactory {

    /**
     * team 类
     */
    private static final String AGENT_TEAM_CLASS = "com.mechempire.agent.Team";

    /**
     * 创建 Team 对象
     *
     * @param agentName jar 包名称
     * @return team 对象
     * @throws Exception 异常
     */
    public static AbstractTeam newTeam(String agentName) throws Exception {
        URLClassLoader classLoader = AgentLoader.getAgentClassLoader(agentName);
        Class<?> agentTeam = classLoader.loadClass(AGENT_TEAM_CLASS);
        AbstractTeam team = (AbstractTeam) agentTeam.newInstance();
        List<AbstractMech> mechList = new ArrayList<>(4);
        for (Class<?> clazz : team.getMechClassList()) {
            AbstractMech mech = (AbstractMech) clazz.newInstance();
            MechFactory.assemblyMech(mech);
            mechList.add(mech);
        }
        team.setMechList(mechList);
        return team;
    }
}