package com.study.day05toolmcp.service;

import com.study.day05toolmcp.tool.CompanyRuleTools;
import com.study.day05toolmcp.tool.CustomerTools;
import com.study.day05toolmcp.tool.DateTimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.stereotype.Service;

@Service
public class ToolChatService {

    private final ChatClient chatClient;
    private final DateTimeTools dateTimeTools;
    private final CustomerTools customerTools;
    private final CompanyRuleTools companyRuleTools;

    public ToolChatService(ChatClient.Builder builder,
                           DateTimeTools dateTimeTools,
                           CustomerTools customerTools,
                           CompanyRuleTools companyRuleTools) {
        this.chatClient = builder
                .defaultAdvisors(new SimpleLoggerAdvisor()) // 내장 logger 추가
                .build();
        this.dateTimeTools = dateTimeTools;
        this.customerTools = customerTools;
        this.companyRuleTools = companyRuleTools;
    }

    public String ask(String question) {
        return chatClient.prompt()
                .tools(dateTimeTools)   // AI가 도구를 손에 쥐게 됨 -> Spring Ai가 처리
                .user(question)
                .call()
                .content();
    }

    public String toolCustomer(String question) {
        return chatClient.prompt()
                .tools(customerTools)
                .user(question)
                .call()
                .content();
    }

    public String toolRule(String question) {
        return chatClient.prompt()
                .tools(companyRuleTools)
                .user(question)
                .call()
                .content();
    }

    // 세 가지 도구 모두 골라서 호출
    public String chat(String question) {
        return chatClient.prompt()
                .tools(dateTimeTools, customerTools, companyRuleTools)
                .user(question)
                .call()
                .content();
    }

}
