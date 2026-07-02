package com.study.day01_chat_client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 요청과 응답만 담당
@RestController // REST API 방식의 컨트롤러
public class ChatController {

    // 서비스 레이어 의존성 주입
    private final ChatService chatService;

    public ChatController(ChatService chatService) {

        this.chatService = chatService;
    }

    @GetMapping("/api/chat")
    public String chat(@RequestParam String message) {

        return chatService.chat(message);
    }

    @GetMapping("/api/teacher")
    public String teacher(@RequestParam String message) {

        return chatService.teacher(message);
    }

    @GetMapping("/api/safe-chat")
    public String safeChat(@RequestParam String message) {

        return chatService.safeChat(message);
    }

    // ===== 미니 도구 4종 엔드포인트 =====

    // 1) 코드 설명 도우미
    // http://localhost:8080/api/code-explain?code=public class Foo { }
    @GetMapping("/api/code-explain")
    public String explainCode(@RequestParam String code) {

        return chatService.explainCode(code);
    }

    // 2) README 생성 도우미
    // http://localhost:8080/api/readme?description=할일 관리 REST API
    @GetMapping("/api/readme")
    public String readme(@RequestParam String description) {

        return chatService.generateReadme(description);
    }

    // 3) 면접 질문 생성기
    // http://localhost:8080/api/interview?techStack=Spring Boot, JPA, MySQL
    @GetMapping("/api/interview")
    public String interview(@RequestParam String techStack) {

        return chatService.generateInterviewQuestions(techStack);
    }

    // 4) 기업 FAQ 응답기
    // http://localhost:8080/api/faq?question=환불은 얼마나 걸리나요
    @GetMapping("/api/faq")
    public String faq(@RequestParam String question) {

        return chatService.answerFaq(question);
    }
}
