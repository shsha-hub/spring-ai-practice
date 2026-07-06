package com.study.day03advisormemory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 메시지 개수가 임계값(summarizeThreshold)을 넘으면,
 * 최근 keepRecentCount개를 제외한 나머지 오래된 메시지들을 LLM으로 요약해서
 * 하나의 UserMessage로 압축한 뒤 저장하는 ChatMemory 구현체.
 *
 * ⚠ SystemMessage가 아니라 UserMessage를 쓰는 이유:
 * Gemini 등 일부 모델 API는 대화 turn(contents)에 system 역할이 섞여 들어오는 것을
 * 허용하지 않는다(system은 오직 별도의 system instruction 필드로만 받음).
 * 요약을 SystemMessage로 저장하면 다음 호출에서 API가 400/500 에러를 던진다.
 *
 * 저장 형태: [요약 UserMessage(있으면 1개)] + [최근 메시지 keepRecentCount개]
 */
public class SummarizingChatMemory implements ChatMemory {

    private static final String SUMMARY_PREFIX = "[이전 대화 요약] ";

    private final ChatMemoryRepository repository;
    private final ConversationSummarizer summarizer;
    private final int keepRecentCount;
    private final int summarizeThreshold;

    public SummarizingChatMemory(ChatMemoryRepository repository,
                                  ConversationSummarizer summarizer,
                                  int keepRecentCount,
                                  int summarizeThreshold) {
        this.repository = repository;
        this.summarizer = summarizer;
        this.keepRecentCount = keepRecentCount;
        this.summarizeThreshold = summarizeThreshold;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> existing = repository.findByConversationId(conversationId);

        List<Message> combined = new ArrayList<>(existing);
        combined.addAll(messages);

        if (combined.size() <= summarizeThreshold) {
            repository.saveAll(conversationId, combined);

            return;
        }

        repository.saveAll(conversationId, compress(combined));
    }

    private List<Message> compress(List<Message> combined) {
        String previousSummary = null;
        List<Message> withoutSummary = combined;

        if (!combined.isEmpty() && isSummaryMessage(combined.get(0))) {
            previousSummary = combined.get(0).getText().replace(SUMMARY_PREFIX, "");
            withoutSummary = combined.subList(1, combined.size());
        }

        int cutIndex = Math.max(0, withoutSummary.size() - keepRecentCount);
        // user/assistant 페어가 어긋나지 않도록 짝수 지점에서 자른다
        if (cutIndex % 2 != 0) {
            cutIndex--;
        }

        List<Message> toSummarize = withoutSummary.subList(0, cutIndex);
        List<Message> toKeep = withoutSummary.subList(cutIndex, withoutSummary.size());

        List<Message> result = new ArrayList<>();

        if (toSummarize.isEmpty()) {
            // 요약할 만큼 쌓이지 않았으면 기존 요약만 유지
            if (previousSummary != null) {
                result.add(new UserMessage(SUMMARY_PREFIX + previousSummary));
            }
            result.addAll(toKeep);

            return result;
        }

        String newSummary = summarizer.summarize(previousSummary, toSummarize);
        result.add(new UserMessage(SUMMARY_PREFIX + newSummary));
        result.addAll(toKeep);

        return result;
    }

    private boolean isSummaryMessage(Message message) {
        return message.getMessageType() == MessageType.USER
                && message.getText() != null
                && message.getText().startsWith(SUMMARY_PREFIX);
    }

    @Override
    public List<Message> get(String conversationId) {
        return repository.findByConversationId(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }

}
