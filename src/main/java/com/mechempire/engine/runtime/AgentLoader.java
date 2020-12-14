package com.mechempire.engine.runtime;

import com.mechempire.sdk.core.game.IMechControlFlow;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 下午4:32
 */
public class AgentLoader {

    /**
     * agent 文件目录
     */
    private static final String AGENT_BASE_PATH = "file:/home/tairy/Documents/Working/mechempire/engine/src/main/resources/agents/";

    /**
     * agent 主类
     */
    private static final String AGENT_MAIN_CLASS = "com.mechempire.agent.AgentMain";

    /**
     * 获取 agent 对象
     *
     * @param agentName agent 名称
     * @return agent 对象
     * @throws Exception 异常
     */
    public static IMechControlFlow getAgentObject(String agentName) throws Exception {
        URL agentFileURL = new URL(AGENT_BASE_PATH + agentName);
        URLClassLoader agentClassLoader =
                new URLClassLoader(new URL[]{agentFileURL}, Thread.currentThread().getContextClassLoader());
        Class<?> agentMainClass = agentClassLoader.loadClass(AGENT_MAIN_CLASS);
        return (IMechControlFlow) agentMainClass.newInstance();
    }
}