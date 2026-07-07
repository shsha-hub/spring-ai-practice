package com.study.day04multimodal.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class MultimodalService {

    private final ChatClient chatClient;

    public MultimodalService(ChatClient.Builder builder) {
        // ChatClientConfig의 defaultAdvisors가 자동 적용됨
        this.chatClient = builder.build();
    }

    /** 1. 구조화된 데이터(DTO) 반환형 파일 분석 메서드 (이미지, PDF 공통) */
    public <T> T analyzeFileToEntity(MultipartFile file, MediaCategory category, String conversationId,
                                      String prompt, Class<T> responseType) {
        validateFile(file, category);
        ByteArrayResource resource = toResource(file);
        MimeType mimeType = MimeType.valueOf(file.getContentType());

        return chatClient.prompt()
                .user(u -> u.text(prompt).media(mimeType, resource))
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .entity(responseType);
    }

    /** 2. 일반 텍스트(String) 반환형 파일 분석 메서드 (이미지, PDF, 오디오 공통) */
    public String analyzeFileToString(MultipartFile file, MediaCategory category, String conversationId,
                                       String prompt) {
        validateFile(file, category);
        ByteArrayResource resource = toResource(file);
        MimeType mimeType = MimeType.valueOf(file.getContentType());

        return chatClient.prompt()
                .user(u -> u.text(prompt).media(mimeType, resource))
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    /** 3. 순수 텍스트 질의 메서드 */
    public String ask(String question, String conversationId) {
        if (question == null || question.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "질문 내용을 입력해주세요.");
        }
        return chatClient.prompt()
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    // ================= 내부 공통 검증 및 변환 메서드 =================

    private ByteArrayResource toResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일 리소스 읽는 중 오류가 발생하였습니다.");
        }
    }

    /**
     * 파일이 비어있지 않은지 + 호출부가 기대한 카테고리(category)와 실제 타입이
     * 일치하는지 검증. 예: /image-analysis에서 category=IMAGE인데 PDF가 오면 막는다.
     */
    private void validateFile(MultipartFile file, MediaCategory category) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    category.label() + "을(를) 업로드해주세요.");
        }

        String contentType = file.getContentType();
        if (!category.accepts(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    category.label() + "만 지원합니다. 받은 타입 : " + contentType);
        }
    }
}
