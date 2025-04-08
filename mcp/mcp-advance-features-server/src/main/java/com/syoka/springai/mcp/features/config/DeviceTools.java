package com.syoka.springai.mcp.features.config;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * @author syoka
 * @version DeviceTools.java, v 0.1 2025-04-08 15:29 syoka
 */
@Service
public class DeviceTools {

    /**
     * 随便注册一个tool
     */
    @Tool(description = "测试工程")
    public String demoTool() {
        return "This is a demo Tool.";
    }

}
