package com.study.day05toolmcp.service;

import com.study.day05toolmcp.mcp.McpToolCatalog;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class McpChatService {

    private final ChatClient chatClient;
    private final McpToolCatalog catalog;

    public McpChatService(ChatClient.Builder builder,
                          McpToolCatalog catalog) {
        this.chatClient = builder.build();
        this.catalog = catalog;
    }

    public String chat(String serverKey, String question) {
        return chatClient.prompt()
                .user(question)
                .tools((Object[]) catalog.tools(serverKey))
                .call()
                .content();
    }

    public String chatAll(String question) {
        return chatClient.prompt()
                .user(question)
                .tools((Object[]) catalog.allTools())
                .call()
                .content();
    }

    public String chatChain(String question) {
        return chatClient.prompt()
                .user(question)
                .tools((Object[]) catalog.toolsExcept("sequential-thinking"))
                .call()
                .content();
    }

}
