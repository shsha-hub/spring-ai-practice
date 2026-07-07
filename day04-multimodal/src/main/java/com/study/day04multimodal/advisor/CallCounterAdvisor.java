package com.study.day04multimodal.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CallCounterAdvisor implements CallAdvisor {

    Logger log = LoggerFactory.getLogger(CallCounterAdvisor.class);
    private final AtomicInteger callCount = new AtomicInteger(0);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse response = chain.nextCall(request);   // 순수 후처리
        this.callCount.incrementAndGet();
        log.info("호출 수 : " + getCallCount());

        return response;
    }

    public int getCallCount() { return this.callCount.get(); }

    // Advisor의 이름
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    // Advisor 호출 우선순위
    // 숫자가 낮은 것부터 실행. 정수 범위의 가장 낮은 수는 실행되지 않음
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;   // 최후에 실행되게 하고 싶은 경우
    }

}
