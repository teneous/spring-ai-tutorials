package com.syoka.springai.mcp.features.config;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Annotations;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于Spring Boot Actuator的系统监控资源配置
 *
 * @author syoka
 */
@Slf4j
@Configuration
public class ExposeResourceAndPromptConfig {

    private final ObjectMapper    objectMapper;
    private final MetricsEndpoint metricsEndpoint;

    @Autowired
    public ExposeResourceAndPromptConfig(ObjectMapper objectMapper,
                                         MetricsEndpoint metricsEndpoint) {
        this.objectMapper = objectMapper;
        this.metricsEndpoint = metricsEndpoint;
    }

    /**
     * Expose Resource
     */
    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> systemResources() {
        return List.of(
                createOsResource(),
                createMemoryResource(),
                createCpuResource(),
                createDiskResource()
        );
    }

    /**
     * Expose Prompt
     *
     * @return 提示词的定义集合
     */
    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> prompts() {
        // 所有prompt均无参数
        var cpuPrompt = new McpSchema.Prompt("查询CPU型号", "查询本机的CPU信息", Collections.emptyList());
        var memPrompt = new McpSchema.Prompt("查询内存大小", "查询本机的内存信息", Collections.emptyList());
        var diskPrompt = new McpSchema.Prompt("查询磁盘容量", "查询本机的磁盘信息", Collections.emptyList());
        var osPrompt = new McpSchema.Prompt("查询系统详细信息", "查询系统操作信息", Collections.emptyList());

        var cpuPromptSpecification = new McpServerFeatures.SyncPromptSpecification(cpuPrompt, (exchange, request) -> {
            var userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER,
                    new McpSchema.TextContent("你是一个计算机专家，你将告知用户当前机器的CPU型号及架构"));
            return new McpSchema.GetPromptResult("查询本机的CPU信息", List.of(userMessage));
        });

        var memPromptSpecification = new McpServerFeatures.SyncPromptSpecification(memPrompt, (exchange, request) -> {
            var userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER,
                    new McpSchema.TextContent("你是一个计算机专家，你将告知用户当前机器的内存容量，并告知内存带宽"));
            return new McpSchema.GetPromptResult("查询本机的内存信息", List.of(userMessage));
        });

        var diskPromptSpecification = new McpServerFeatures.SyncPromptSpecification(diskPrompt, (exchange, request) -> {
            var userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER,
                    new McpSchema.TextContent("你是一个计算机专家，你将告知用户当前机器的磁盘大小"));
            return new McpSchema.GetPromptResult("查询本机的磁盘", List.of(userMessage));
        });

        var osPromptSpecification = new McpServerFeatures.SyncPromptSpecification(osPrompt, (exchange, request) -> {
            var userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER,
                    new McpSchema.TextContent("你是一个计算机专家，你将告知用户当前机器的系统信息,是win,mac,linux还是什么，还有对应的系统版本"));
            return new McpSchema.GetPromptResult("查询本机的系统操作信息", List.of(userMessage));
        });

        // MCP服务暴露了4个prompt
        return List.of(cpuPromptSpecification, memPromptSpecification, diskPromptSpecification, osPromptSpecification);
    }


    private McpServerFeatures.SyncResourceSpecification createOsResource() {
        var resource = new McpSchema.Resource(
                // 业务自定义资源定位符
                "device://localhost/os",
                "操作系统信息",
                "获取当前操作系统的基本信息",
                "application/json",
                new Annotations(List.of(McpSchema.Role.USER, McpSchema.Role.ASSISTANT), 0.6d)
        );

        return new McpServerFeatures.SyncResourceSpecification(
                resource,
                (exchange, request) -> {
                    try {
                        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                        Map<String, Object> osInfo = Map.of(
                                "name", System.getProperty("os.name"),
                                "version", System.getProperty("os.version"),
                                "arch", System.getProperty("os.arch"),
                                "processors", osBean.getAvailableProcessors(),
                                "systemLoadAverage", osBean.getSystemLoadAverage()
                        );

                        String jsonContent = objectMapper.writeValueAsString(osInfo);
                        return new McpSchema.ReadResourceResult(
                                List.of(new McpSchema.TextResourceContents(
                                        request.uri(),
                                        "application/json",
                                        jsonContent
                                ))
                        );
                    } catch (Exception e) {
                        log.error("获取操作系统信息失败", e);
                        throw new RuntimeException("获取操作系统信息失败", e);
                    }
                }
        );
    }

    private McpServerFeatures.SyncResourceSpecification createMemoryResource() {
        var resource = new McpSchema.Resource(
                "device://localhost/mem",
                "内存使用情况",
                "获取系统内存和JVM内存使用详情",
                "application/json",
                new Annotations(List.of(McpSchema.Role.USER, McpSchema.Role.ASSISTANT), 0.6d)
        );

        return new McpServerFeatures.SyncResourceSpecification(
                resource,
                (exchange, request) -> {
                    try {
                        var memoryInfo = Map.of(
                                "jvm", Map.of(
                                        "heap", getHeapMemoryInfo(),
                                        "nonHeap", getNonHeapMemoryInfo()
                                )
                        );

                        String jsonContent = objectMapper.writeValueAsString(memoryInfo);
                        return new McpSchema.ReadResourceResult(
                                List.of(new McpSchema.TextResourceContents(
                                        request.uri(),
                                        "application/json",
                                        jsonContent
                                ))
                        );
                    } catch (Exception e) {
                        log.error("获取内存信息失败", e);
                        throw new RuntimeException("获取内存信息失败", e);
                    }
                }
        );
    }

    private McpServerFeatures.SyncResourceSpecification createCpuResource() {
        var resource = new McpSchema.Resource(
                "device://localhost/cpu",
                "CPU使用情况",
                "获取CPU使用率和负载信息",
                "application/json",
                new Annotations(List.of(McpSchema.Role.USER, McpSchema.Role.ASSISTANT), 0.6d)

        );

        return new McpServerFeatures.SyncResourceSpecification(
                resource,
                (exchange, request) -> {
                    try {
                        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                        Map<String, Object> cpuInfo = Map.of(
                                "processCpuLoad", osBean.getProcessCpuLoad() * 100,
                                "systemCpuLoad", osBean.getCpuLoad() * 100,
                                "systemLoadAverage", osBean.getSystemLoadAverage()
                        );

                        String jsonContent = objectMapper.writeValueAsString(cpuInfo);
                        return new McpSchema.ReadResourceResult(
                                List.of(new McpSchema.TextResourceContents(
                                        request.uri(),
                                        "application/json",
                                        jsonContent
                                ))
                        );
                    } catch (Exception e) {
                        log.error("获取CPU信息失败", e);
                        throw new RuntimeException("获取CPU信息失败", e);
                    }
                }
        );
    }

    private McpServerFeatures.SyncResourceSpecification createDiskResource() {
        var resource = new McpSchema.Resource(
                "device://localhost/disk",
                "磁盘使用情况",
                "获取系统磁盘空间使用情况",
                "application/json",
                new Annotations(List.of(McpSchema.Role.USER, McpSchema.Role.ASSISTANT), 0.6d)
        );

        return new McpServerFeatures.SyncResourceSpecification(
                resource,
                (exchange, request) -> {
                    try {
                        var diskInfo = metricsEndpoint.metric("disk.total", null);

                        String jsonContent = objectMapper.writeValueAsString(diskInfo);
                        return new McpSchema.ReadResourceResult(
                                List.of(new McpSchema.TextResourceContents(
                                        request.uri(),
                                        "application/json",
                                        jsonContent
                                ))
                        );
                    } catch (Exception e) {
                        log.error("获取磁盘信息失败", e);
                        throw new RuntimeException("获取磁盘信息失败", e);
                    }
                }
        );
    }

    private Map<String, Object> getHeapMemoryInfo() {
        var heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        return Map.of(
                "init", heap.getInit(),
                "used", heap.getUsed(),
                "committed", heap.getCommitted(),
                "max", heap.getMax()
        );
    }

    private Map<String, Object> getNonHeapMemoryInfo() {
        var nonHeap = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        return Map.of(
                "init", nonHeap.getInit(),
                "used", nonHeap.getUsed(),
                "committed", nonHeap.getCommitted(),
                "max", nonHeap.getMax()
        );
    }
}
