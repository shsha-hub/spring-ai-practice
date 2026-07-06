package com.study.day03advisormemory;

import com.study.day03advisormemory.advisor.CallCounterAdvisor;
import com.study.day03advisormemory.dto.MemoryMessageView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AiController {

    private final SummaryChatService summaryChatService;
    private final CallCounterAdvisor callCounterAdvisor;

    public AiController(SummaryChatService summaryChatService, CallCounterAdvisor callCounterAdvisor) {
        this.summaryChatService = summaryChatService;
        this.callCounterAdvisor = callCounterAdvisor;
    }

    @GetMapping("/api/chat-summary")
    public String chatSummary(@RequestParam String question,
                              @RequestParam String conversationID) {
        return summaryChatService.chat(question, conversationID);
    }

    // 프론트에서 "현재 저장소에 실제로 남아있는 메시지" 상태를 보여주기 위한 조회용 엔드포인트
    @GetMapping("/api/chat-summary/history")
    public List<MemoryMessageView> history(@RequestParam String conversationID) {
        return summaryChatService.getMemorySnapshot(conversationID);
    }

    // 대화 초기화 (같은 conversationID로 새로 테스트하고 싶을 때)
    @GetMapping("/api/chat-summary/clear")
    public void clear(@RequestParam String conversationID) {
        summaryChatService.clear(conversationID);
    }

    @GetMapping("/api/call-count")
    public int callCount() {
        return callCounterAdvisor.getCallCount();
    }

}
