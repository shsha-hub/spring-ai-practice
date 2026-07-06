package com.study.day03advisormemory;

import com.study.day03advisormemory.advisor.CallCounterAdvisor;
import com.study.day03advisormemory.dto.MemoryMessageView;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummaryChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public SummaryChatService(ChatClient.Builder builder,
                              @Qualifier("summarizingChatMemory") ChatMemory chatMemory,
                              CallCounterAdvisor callCounterAdvisor) {
        this.chatMemory = chatMemory;
        this.chatClient = builder
                .defaultSystem("""
                        당신은 사내 개발팀을 돕는 AI assistant이다.
                        정중하고, 간결한 한국어로 답변하시오.
                        """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        callCounterAdvisor
                )
                .build();
    }

    public String chat(String question, String conversationID) {
        return chatClient.prompt()
                .user(question)
                .advisors(spec -> spec.param(
                        ChatMemory.CONVERSATION_ID, conversationID
                ))
                .call()
                .content();
    }

    // 프론트에서 "현재 저장된 메모리 상태"를 그대로 보여주기 위한 조회용 메서드
    public List<MemoryMessageView> getMemorySnapshot(String conversationID) {
        return chatMemory.get(conversationID).stream()
                .map(m -> new MemoryMessageView(m.getMessageType().name(), m.getText()))
                .toList();
    }

    public void clear(String conversationID) {
        chatMemory.clear(conversationID);
    }

}
