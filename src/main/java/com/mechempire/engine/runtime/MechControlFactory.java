package com.mechempire.engine.runtime;

import com.mechempire.sdk.core.game.IMechControlFlow;

import java.net.URLClassLoader;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/15 下午5:39
 */
public class MechControlFactory {

    /**
     * agent 主控制类
     */
    private static final String AGENT_MAIN_CLASS = "com.mechempire.agent.AgentMain";

    /**
     * 获取机甲主控制流类
     *
     * @param agentName jar 名称
     * @return 机甲控制流
     * @throws Exception 异常
     */
    public static IMechControlFlow getTeamControl(String agentName) throws Exception {
        URLClassLoader classLoader = AgentLoader.getAgentClassLoader(agentName);
        Class<?> agentTeam = classLoader.loadClass(AGENT_MAIN_CLASS);
        return (IMechControlFlow) agentTeam.newInstance();
    }
}