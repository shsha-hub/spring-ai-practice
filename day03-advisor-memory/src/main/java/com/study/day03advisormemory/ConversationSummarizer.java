package com.study.day03advisormemory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 오래된 메시지 목록(+ 기존 요약)을 받아서 LLM으로 짧은 요약문을 생성한다.
 * 메인 대화용 ChatClient와는 별개의, 요약 전용 ChatClient를 사용한다.
 */
public class ConversationSummarizer {

    private final ChatClient summarizerClient;

    public ConversationSummarizer(ChatClient.Builder builder) {
        this.summarizerClient = builder
                .defaultSystem("""
                        너는 대화 내용을 압축 요약하는 도우미다.
                        아래 대화 내용을 핵심 사실 위주로 3~5문장 이내 한국어로 요약하라.
                        사용자의 이름, 취향, 이전에 요청한 내용처럼 나중에 다시 참조될 만한
                        정보는 반드시 포함하라. 불필요한 인사말이나 부연설명은 넣지 마라.
                        """)
                .build();
    }

    public String summarize(String previousSummary, List<Message> messagesToCompress) {
        String transcript = messagesToCompress.stream()
                .map(m -> m.getMessageType() + ": " + m.getText())
                .collect(Collectors.joining("\n"));

        String userPrompt = (previousSummary != null
                ? "기존 요약:\n" + previousSummary + "\n\n"
                : "")
                + "새로 추가된 대화:\n" + transcript;

        return summarizerClient.prompt()
                .user(userPrompt)
                .call()
                .content();
    }

}
