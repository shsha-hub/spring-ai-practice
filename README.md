# Day1 - Spring AI ChatClient 미니 도구

Spring AI의 `ChatClient`로 Gemini와 대화하는 백엔드를 만들고, 그 위에 실용적인 미니 AI 도구 4종을 붙여본 실습 프로젝트입니다.

## 이번 실습에서 배운 것

- `ChatClient.Builder`가 자동구성되어 별도 `@Bean` 등록 없이 주입받아 사용
- `.system()` + `.user()` + `.call()` + `.content()` 체이닝으로 AI 모델 호출
- `system` 프롬프트로 역할을 부여하면 같은 질문도 응답 톤·구조가 달라짐
- `ChatOptions`로 `temperature` 등 모델 옵션 조정
- Controller(요청/응답) - Service(비즈니스 로직) 계층 분리

## 사용 기술

| 항목 | 버전 |
|---|---|
| JDK | 21 LTS |
| Spring Boot | 4.1.x |
| Spring AI | 2.0.0 GA |
| Provider | Google GenAI (Gemini 3.1 Flash Lite) |
| Template Engine | Thymeleaf |

## 프로젝트 구조

```
src/main/java/com/study/day01_chat_client/
├── Day01ChatClientApplication.java   # 메인 애플리케이션
├── ChatController.java               # REST API 컨트롤러
├── ChatViewController.java           # 화면(HTML) 컨트롤러
└── ChatService.java                  # ChatClient 비즈니스 로직

src/main/resources/
├── templates/chat.html               # 미니 도구 웹 UI
└── application.yml                   # Gemini API 설정
```

## 엔드포인트 목록

### 기본 챗

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/chat?message=` | 기본 응답 |
| GET | `/api/teacher?message=` | 선생님 역할 부여 응답 |
| GET | `/api/safe-chat?message=` | temperature 낮춘(0.2) 안정적인 응답 |

### 미니 도구 4종

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/code-explain?code=` | 코드 스니펫 → 단계별 설명 |
| GET | `/api/readme?description=` | 프로젝트 설명 → README 초안 |
| GET | `/api/interview?techStack=` | 기술 스택 → 난이도별 예상 면접 질문 |
| GET | `/api/faq?question=` | 자주 묻는 질문 → 답변 초안 |

### 화면

| Method | URL | 설명 |
|---|---|---|
| GET | `/chat` | 미니 도구 4종을 테스트할 수 있는 웹 UI |

## 실행 방법

1. Google AI Studio에서 API 키 발급

2. 환경변수로 API 키 설정

   ```bash
   # macOS / Linux
   export GOOGLE_API_KEY=발급받은_키

   # Windows (PowerShell)
   $env:GOOGLE_API_KEY="발급받은_키"
   ```

3. 애플리케이션 실행

   ```bash
   ./gradlew bootRun
   ```

4. 브라우저에서 접속

   ```
   http://localhost:8080/chat
   ```

## 응답 캡처

<!--
아래 자리에 스크린샷을 넣으세요.
1) 캡처 이미지를 프로젝트 폴더(예: docs/images/)에 저장
2) 아래처럼 마크다운 이미지 문법으로 삽입
   ![코드 설명 도우미 응답 예시](docs/images/code-explain.png)
-->

### 1. 코드 설명 도우미

![코드 설명 도우미 응답 예시](docs/images/code-explain.png)

### 2. README 생성 도우미

![README 생성 도우미 응답 예시](docs/images/readme-explain.png)

## 막혔을 때 체크리스트

1. `javac -version`이 뜨는가 (JDK 설치 확인)
2. 서버가 `Started ... Application`까지 로그에 찍혔는가
3. 호출이 500이면 로그에 `API_KEY_INVALID`가 보이는가 → 환경변수 재확인
4. `application.yml`에 탭 문자가 섞이지 않았는가
5. URL 파라미터 이름이 `@RequestParam` 변수명과 일치하는가
