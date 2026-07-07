package com.study.day04multimodal.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO: Day3에서 직접 만든 CallCounterAdvisor를 여기로 import
// import com.study.day04multimodal.advisor.CallCounterAdvisor;

/**
 * Day1~3에서 각각 따로 확인했던 것들을 한 곳에서 조립하는 설정 클래스.
 * - ChatMemory: 대화 히스토리 저장소 (기본은 인메모리, 재시작하면 날아감)
 * - ChatClient.Builder: 모든 컨트롤러가 공유하는 빌더, 기본 Advisor 3종 장착
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor()
                );
    }

}
