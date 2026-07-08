package com.study.day05toolmcp.controller;

import com.study.day05toolmcp.service.ChatService;
import com.study.day05toolmcp.service.HelpdeskService;
import com.study.day05toolmcp.service.McpChatService;
import com.study.day05toolmcp.service.ToolChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {

    private final ChatService chatService;
    private final ToolChatService toolChatService;
    private final HelpdeskService helpdeskService;
    private final McpChatService mcpChatService;

    public AiController(ChatService chatService,
                        ToolChatService toolChatService,
                        HelpdeskService helpdeskService,
                        McpChatService mcpChatService) {
        this.chatService = chatService;
        this.toolChatService = toolChatService;
        this.helpdeskService = helpdeskService;
        this.mcpChatService = mcpChatService;
    }

    @GetMapping("/api/ask")
    public String ask(@RequestParam String question) {
        return chatService.ask(question);
    }

    @GetMapping("/api/tool/datetime")
    public String toolDatetime(@RequestParam String question) {
        return toolChatService.ask(question);
    }

    @GetMapping("/api/tool/customer")
    public String toolCustomer(@RequestParam String question) {
        return toolChatService.toolCustomer(question);
    }

    @GetMapping("/api/tool/rule")
    public String toolRule(@RequestParam String question) {
        return toolChatService.toolRule(question);
    }

    @GetMapping("/api/tool/chat")
    public String toolChat(@RequestParam String question) {
        return toolChatService.chat(question);
    }

    @GetMapping("/api/tool/help")
    public String helpdesk(@RequestParam String question,
                           @RequestParam String conversationId) {
        return helpdeskService.chat(question, conversationId);
    }

    @GetMapping("/api/mcp/{serverKey}")
    public String mcp(@PathVariable String serverKey, @RequestParam String question) {
        return mcpChatService.chat(serverKey, question);
    }

    @GetMapping("/api/mcp-chat")
    public String mcpChat(@RequestParam String question) {
        return mcpChatService.chatAll(question);
    }

    @GetMapping("/api/mcp-chain")
    public String mcpChain(@RequestParam String question) {
        return mcpChatService.chatChain(question);
    }

}
