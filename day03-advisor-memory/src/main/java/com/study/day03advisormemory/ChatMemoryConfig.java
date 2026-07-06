package com.study.day03advisormemory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    // ChatMemoryRepository(JDBC 구현체)는 spring-ai-starter-model-chat-memory-repository-jdbc가
    // application.yml 설정을 보고 자동으로 빈 등록해준다 - 직접 만들 필요 없음

    // 비교용 - 요약 없이 최근 20개만 유지하는 기본 방식
    @Bean("plainJdbcChatMemory")
    public ChatMemory plainJdbcChatMemory(ChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(20)
                .build();
    }

    @Bean
    public ConversationSummarizer conversationSummarizer(ChatClient.Builder builder) {
        return new ConversationSummarizer(builder);
    }

    // 이번 프로젝트의 핵심 - 오래된 메시지를 요약해서 보관하는 방식
    @Bean("summarizingChatMemory")
    public ChatMemory summarizingChatMemory(ChatMemoryRepository repository,
                                             ConversationSummarizer summarizer) {
        return new SummarizingChatMemory(repository, summarizer,
                6,   // keepRecentCount - 요약 안 하고 그대로 남길 최근 메시지 수
                12); // summarizeThreshold - 이 개수 넘으면 요약 트리거
    }

}
