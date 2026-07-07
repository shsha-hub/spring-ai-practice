package com.study.day04multimodal.config;

import com.google.genai.Client;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Spring AI의 GoogleGenAiChatModel은 내부적으로 com.google.genai.Client를 감싸지만
 * 그 Client를 빈으로 꺼내 쓸 방법은 없다. Imagen(이미지 생성)·TTS·Live API는
 * Spring AI ChatClient가 감싸는 범위 밖(§9 심화)이라 Google 공식 SDK Client를
 * 별도로 하나 더 만들어 직접 호출한다. API 키는 기존 ChatModel과 동일한
 * spring.ai.google.genai.api-key 값을 재사용한다.
 *
 * 주의: com.google.genai.Client 타입을 그대로 @Bean으로 등록하면 spring-ai-google-genai
 * 자동 구성이 내부적으로 참조하는 Client 타입과 충돌해 두 Client 모두 컨테이너 초기화
 * 극초반(환경 프로퍼티가 아직 다 붙기 전)에 강제로 만들어지는 문제가 있었다
 * (증상: 이 Bean의 @Value 주입 값과 Environment.getProperty() 조회 값이 서로 달랐고,
 * 기존 /api/ask까지 함께 API_KEY_INVALID로 깨졌다). 그래서 Client를 Bean으로 노출하지 않고
 * 이 Component가 지연 생성해 보관하는 방식으로 우회한다.
 */
@Component
public class GenAiClientConfig {

    private final Client client;

    public GenAiClientConfig(Environment environment) {
        String apiKey = environment.getRequiredProperty("spring.ai.google.genai.api-key");
        this.client = Client.builder().apiKey(apiKey).build();
    }

    public Client client() {
        return client;
    }
}
