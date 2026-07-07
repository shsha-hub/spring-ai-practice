package com.study.day04multimodal.controller;

import java.util.Base64;

import com.study.day04multimodal.dto.SpeechGenerationResponse;
import com.study.day04multimodal.dto.SpeechRequest;
import com.study.day04multimodal.service.SpeechGenerationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day4 심화·선택 실습: Gemini TTS. §2~§8의 필수 미션과 별도로 묶어 둔 이유는,
 * Spring AI ChatClient가 아니라 Google 공식 SDK(com.google.genai.Client)를 직접
 * 호출하는 코드라서 기존 ApiController의 "ChatClient 재사용" 흐름과 결이 다르기 때문이다.
 * (Imagen 이미지 생성·Gemini Live·Live Translate도 함께 시도했으나, 무료 티어 거부·
 * 응답 미수신 등으로 실습에 쓰기엔 불안정해 제거했다 — _plan.md 참고.)
 * 응답은 base64 WAV 문자열로 감싸 반환한다. Thymeleaf 확인 화면(multimodal.html)에서
 * "data:audio/wav;base64,..." data URL로 바로 재생할 수 있다.
 */
@RestController
public class SpeechGenerationController {

    private final SpeechGenerationService speechGenerationService;

    public SpeechGenerationController(SpeechGenerationService speechGenerationService) {
        this.speechGenerationService = speechGenerationService;
    }

    /** Gemini TTS로 텍스트를 음성(WAV)으로 합성한다. voiceName이 비어 있으면 "Kore"를 기본값으로 쓴다. */
    @PostMapping("/api/speech-generation")
    public SpeechGenerationResponse speechGeneration(@RequestBody SpeechRequest request) {
        String voiceName = (request.voiceName() == null || request.voiceName().isBlank())
                ? "Kore" : request.voiceName();
        byte[] wav = speechGenerationService.speak(request.text(), voiceName);

        return new SpeechGenerationResponse(Base64.getEncoder().encodeToString(wav));
    }

}
