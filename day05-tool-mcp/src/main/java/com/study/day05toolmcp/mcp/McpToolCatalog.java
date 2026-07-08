package com.study.day05toolmcp.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class McpToolCatalog {

    private static final List<String> SERVER_KEYS = List.of("filesystem", "fetch", "sequential-thinking");
    private final Map<String, ToolCallback[]> toolsByServer;
    private final ToolCallback[] allTools;

    public McpToolCatalog(List<McpSyncClient> mcpClients) {
        this.allTools = toolsFrom(mcpClients);
        this.toolsByServer = SERVER_KEYS.stream()
                .collect(Collectors.toMap(
                        key -> key,                               // 키는 그대로 문자열
                        key -> toolsFrom(named(mcpClients, key))  // 값은 그 서버의 도구 배열
                ));
    }

    public ToolCallback[] tools(String serverKey) {
        return toolsByServer.getOrDefault(serverKey, new ToolCallback[0]);
    }

    public ToolCallback[] allTools() {
        return allTools;
    }

    public ToolCallback[] toolsExcept(String... excludeKeys) {
        List<String> excluded = List.of(excludeKeys);
        return SERVER_KEYS.stream()
                .filter(key -> !excluded.contains(key))     // 제외할 키가 아닌 것만
                .flatMap(key -> Arrays.stream(tools(key)))  // 각 서버의 도구 배열 -> 하나의 스트림으로 펼침
                .toArray(ToolCallback[]::new);              // 다시 배열로 모음
    }

    private List<McpSyncClient> named(List<McpSyncClient> clients,
                                      String serverName) {
        return clients.stream()
                .filter(client -> client.getServerInfo()
                        .name()
                        .contains(serverName))
                .toList();
    }

    private ToolCallback[] toolsFrom(List<McpSyncClient> clients) {
        return SyncMcpToolCallbackProvider.builder()
                .mcpClients(clients)
                .build()
                .getToolCallbacks();
    }

}