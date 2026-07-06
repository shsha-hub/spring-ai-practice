package com.study.day03advisormemory.advisor;

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

    private final Logger log = LoggerFactory.getLogger(CallCounterAdvisor.class);
    private final AtomicInteger callCount = new AtomicInteger(0);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse response = chain.nextCall(request);   // 순수 후처리
        this.callCount.incrementAndGet();
        log.info("호출 수: {}", getCallCount());
        
        return response;
    }

    public int getCallCount() {
        return this.callCount.get();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;   // 최후에 실행
    }

}
