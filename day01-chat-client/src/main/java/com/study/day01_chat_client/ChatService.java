package com.study.day01_chat_client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service    // 컴포넌트 선언
public class ChatService {

    // Spring AI ChatClient 객체
    private final ChatClient chatClient;

    // Spring Boot가 구성해줄 수 있도록 builder 패턴으로 주입 (DI)
    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // 비즈니스 로직 AI에 연결하는
    // 일반 응답
    public String chat(@RequestParam String message) {
        // http://localhost:8080/api/chat?message=내용
        return chatClient.prompt()      // 선언 시작점
                         .user(message) // User Prompt : 사용자가 실제 입력한 질문
                         .call()        // 호출 -> 응답할 때까지 기다림
                         .content();    // "응답 텍스트 본문만 가져와줘"
    }

    // 교사 역할 응답
    public String teacher(@RequestParam String message) {
        return chatClient.prompt()
                         .system("""
                                 당신은 Java, Spring Boot, Spring AI를 가르치는 선생님입니다.
                                 초보 학습자에게 설명하듯이 답변해주세요.
                                 핵심 개념, 예시, 주의할 점을 포함하여야 합니다.
                                 친절하게 한국어로 설명해주세요.
                                 """)   // System Prompt : 역할 지시
                         .user(message)
                         .call()
                         .content();
    }

    // 기준을 강화한 옵션 챗
    public String safeChat(String message) {
        return chatClient.prompt()
                         .user(message)
                         .options(GoogleGenAiChatOptions.builder()
                                 .temperature(0.2)) // 기준 강화
                         .call()
                         .content();
    }

    // ===== 여기서부터 미니 도구 4종 =====

    // 1) 코드 설명 도우미 : 코드 스니펫 -> 설명
    public String explainCode(String code) {
        // http://localhost:8080/api/code-explain?code=코드내용
        return chatClient.prompt()
                         .system("""
                                 당신은 코드를 쉽게 설명해주는 시니어 개발자입니다.
                                 아래 규칙을 반드시 지켜서 설명하세요.

                                 1. 이 코드가 전체적으로 어떤 일을 하는지 한두 문장으로 요약
                                 2. 핵심 로직을 위에서부터 순서대로 단계별로 설명
                                 3. 초보자가 헷갈릴 수 있는 문법이나 개념이 있으면 짚어줄 것
                                 4. 잠재적인 버그나 주의할 점이 있으면 마지막에 언급
                                 5. 한국어로, 친절하지만 장황하지 않게 답변
                                 """)
                         .user("다음 코드를 설명해줘:\n\n" + code)
                         .call()
                         .content();
    }

    // 2) README 생성 도우미 : 프로젝트 설명 -> README 초안
    public String generateReadme(String description) {
        // http://localhost:8080/api/readme?description=프로젝트설명
        return chatClient.prompt()
                         .system("""
                                 당신은 GitHub README.md 작성을 전문으로 하는 개발자입니다.
                                 사용자가 프로젝트에 대한 간단한 설명을 주면,
                                 아래 섹션을 포함한 README.md 초안을 마크다운 형식으로 작성하세요.

                                 - 프로젝트 소개 (한두 문단)
                                 - 주요 기능 (불릿 리스트)
                                 - 기술 스택
                                 - 설치 및 실행 방법 (예시 커맨드 포함)
                                 - 사용 예시

                                 실제로 존재하지 않는 구체적인 버전 번호나 없는 사실을 지어내지 말고,
                                 사용자가 채워 넣을 수 있도록 [ ] 형태의 자리표시자를 적절히 활용하세요.
                                 """)
                         .user("다음 설명을 바탕으로 README를 작성해줘:\n\n" + description)
                         .call()
                         .content();
    }

    // 3) 면접 질문 생성기 : 기술 스택 -> 예상 면접 질문
    public String generateInterviewQuestions(String techStack) {
        // http://localhost:8080/api/interview?techStack=Spring Boot, JPA
        return chatClient.prompt()
                         .system("""
                                 당신은 백엔드 개발자 채용 면접관입니다.
                                 주어진 기술 스택을 기반으로 예상 면접 질문 5개를 만들어주세요.

                                 형식:
                                 1. [난이도: 초급/중급/고급] 질문 내용
                                    - 이 질문의 출제 의도를 한 줄로 설명

                                 난이도는 초급 2개, 중급 2개, 고급 1개로 구성하고,
                                 실무에서 실제로 물어볼 법한 현실적인 질문으로 작성하세요.
                                 """)
                         .user("기술 스택: " + techStack)
                         .call()
                         .content();
    }

    // 4) 기업 FAQ 응답기 : 자주 묻는 질문 -> 답변 초안
    public String answerFaq(String question) {
        // http://localhost:8080/api/faq?question=환불은어떻게하나요
        return chatClient.prompt()
                         .system("""
                                 당신은 스타트업 고객지원팀의 답변 초안을 작성하는 담당자입니다.
                                 고객이 자주 묻는 질문을 주면, 아래 톤과 구조로 답변 초안을 작성하세요.

                                 - 정중하고 친절한 존댓말 사용
                                 - 첫 문장에서 질문에 대한 핵심 답변을 바로 제시
                                 - 필요하면 절차나 조건을 번호 목록으로 안내
                                 - 회사명, 구체적 정책 등 실제로 모르는 정보는 지어내지 말고
                                   "[회사 정책에 따라 다를 수 있습니다]" 같은 자리표시자로 표시
                                 - 마지막에 추가 문의 안내 한 줄 포함
                                 """)
                         .user(question)
                         .call()
                         .content();
    }
}
