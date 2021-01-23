package com.mechempire.engine.runtime;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * package: com.mechempire.engine.runtime
 *
 * @author <tairy> tairyguo@gmail.com
 * @date 2020/12/14 下午4:32
 */
class AgentLoader {
    /**
     * agent 文件目录
     */
    private static final String AGENT_BASE_PATH = "file:/Users/tairy/Documents/Working/mechempire/engine/src/main/resources/agents/";

    /**
     * 类加载器缓存
     */
    private static final Map<String, URLClassLoader> loaderCache = new HashMap<>(2);

    /**
     * 加载 jar 包, 并返回类加载器
     *
     * @param agentName jar 包名称
     * @return 类加载器
     * @throws Exception 异常
     */
    static URLClassLoader getAgentClassLoader(String agentName) throws Exception {
        URLClassLoader classLoader = loaderCache.get(agentName);
        if (null == classLoader) {
            URL agentFileURL = new URL(AGENT_BASE_PATH + agentName);
            classLoader = new URLClassLoader(new URL[]{agentFileURL}, Thread.currentThread().getContextClassLoader());
            loaderCache.put(agentName, classLoader);
        }
        return classLoader;
    }
}