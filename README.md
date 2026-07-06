#### 긴 대화 자동 요약기 — 실행 안내

#### 1. 프로젝트 구조

```
build.gradle
src/main/resources/
  ├─ application.yml
  └─ static/
      └─ index.html                        (테스트용 채팅 화면)

src/main/java/com/study/summarizingmemory/
  ├─ SummarizingMemoryApplication.java      (메인 클래스)
  ├─ ChatMemoryConfig.java                  (ChatMemory 빈 등록)
  ├─ ConversationSummarizer.java            (요약 전담 LLM 호출)
  ├─ SummarizingChatMemory.java             (핵심 - 요약형 ChatMemory 구현체)
  ├─ SummaryChatService.java                (대화 서비스 + 메모리 조회/초기화)
  ├─ AiController.java                      (REST 엔드포인트)
  ├─ dto/
  │   └─ MemoryMessageView.java             (메모리 상태 응답 DTO)
  └─ advisor/
      └─ CallCounterAdvisor.java            (호출 횟수 카운터)
```

패키지는 `dto`, `advisor` 두 개만 분리했고, 나머지 핵심 클래스(Config/Service/Controller/Memory)는 루트 패키지에 그대로 둔 상태입니다.

#### 2. 시작 전에 확인할 것

- `build.gradle`의 모델 starter를 실제 사용 중인 provider에 맞게 바꾸세요 (기본값: Gemini/google-genai)
- `application.yml`의 `api-key` 값을 환경 변수로 채워주세요
  ```bash
  export GOOGLE_API_KEY=여기에_키
  ```

#### 3. 실행

```bash
./gradlew bootRun
```

브라우저에서 바로 테스트하려면:
```
http://localhost:8080/index.html
```

#### 4. API 엔드포인트

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/chat-summary?question=...&conversationID=...` | 질문에 대한 응답 (요약형 메모리 적용) |
| GET | `/api/chat-summary/history?conversationID=...` | 현재 저장소에 남아있는 메시지 목록 조회 (요약 메시지 포함) |
| GET | `/api/chat-summary/clear?conversationID=...` | 해당 conversationID의 대화 기억 초기화 |
| GET | `/api/call-count` | `CallCounterAdvisor` 누적 호출 횟수 조회 |

#### 5. 화면 구성 (index.html)

- 왼쪽: 채팅창 — 질문을 입력하면 바로 응답을 확인
- 오른쪽: "저장된 메모리 상태" — `ChatMemoryRepository`에 실제로 남아있는 메시지를 그대로 보여줌. `SYSTEM` 타입이면서 `[이전 대화 요약]`으로 시작하는 메시지는 주황색 `SUMMARY` 카드로 강조 표시
- 상단: `conversationID` 변경/불러오기, 대화 초기화, `call-count` 실시간 표시

#### 6. 테스트 시나리오 (핵심 증거 만들기)

화면에서 같은 `conversationID`로 10턴 이상 대화를 이어가면서, 초반에 말한 내용을 한참 뒤에 다시 물어보면 요약이 잘 동작하는지 확인할 수 있습니다.

브라우저 대신 curl로 확인하고 싶다면:

```bash
CONV="summary-demo-01"
BASE="http://localhost:8080/api/chat-summary"

curl -G "$BASE" --data-urlencode "question=내 이름은 지훈이야. 취미는 등산이고 요즘 사이드 프로젝트로 스프링 AI 공부 중이야." --data-urlencode "conversationID=$CONV"
curl -G "$BASE" --data-urlencode "question=오늘 날씨 어때?" --data-urlencode "conversationID=$CONV"
curl -G "$BASE" --data-urlencode "question=자바 17이랑 21 차이 알려줘." --data-urlencode "conversationID=$CONV"
curl -G "$BASE" --data-urlencode "question=스프링 부트 3.4는 언제 나왔어?" --data-urlencode "conversationID=$CONV"
curl -G "$BASE" --data-urlencode "question=REST API 설계 원칙 3가지만 알려줘." --data-urlencode "conversationID=$CONV"
curl -G "$BASE" --data-urlencode "question=그럼 페이징은 어떻게 설계하는 게 좋을까?" --data-urlencode "conversationID=$CONV"
curl -G "$BASE" --data-urlencode "question=좋아, 고마워." --data-urlencode "conversationID=$CONV"

# 여기서부터 요약이 트리거될 가능성이 높습니다
curl -G "$BASE" --data-urlencode "question=내 이름이 뭐였지? 그리고 취미도 같이 말해줘." --data-urlencode "conversationID=$CONV"

# 저장소 상태 직접 확인
curl -G "http://localhost:8080/api/chat-summary/history" --data-urlencode "conversationID=$CONV"
```

마지막 질문에서 이름(지훈)과 취미(등산)를 정확히 답하면 성공입니다 — 원본 메시지는 이미 삭제됐어도, 요약을 통해 여전히 기억하고 있다는 뜻이니까요.

#### 7. 관찰 포인트

- `logging.level.com.study.summarizingmemory: DEBUG` 설정 덕분에, 콘솔에서 몇 번째 호출에서 요약이 발생하는지 확인 가능
- `/api/call-count`로 호출 횟수를 확인하면, 질문 1번당 실제로는 **본 응답 1번 + (요약 트리거 시) 요약 호출 1번**, 총 2번의 LLM 호출이 발생할 수 있다는 점을 관찰할 수 있습니다
- `index.html`의 오른쪽 패널이 사실상 `chatMemory.get(conversationID)`를 그대로 시각화한 것이라, DB 파일을 직접 열어보지 않아도 저장 상태를 눈으로 확인할 수 있습니다

#### 8. 알려진 한계 / 트러블슈팅 기록

- **[해결됨] 요약 트리거 직후 500 에러** — 처음엔 요약을 `SystemMessage`로 저장했는데, Gemini(Google GenAI) API는 대화 turn(contents)에 system 역할이 섞여 들어오는 걸 허용하지 않아서(system은 오직 `systemInstruction` 필드로만 받음) 요약이 생긴 다음 질문부터 계속 500 에러가 났습니다. → `SystemMessage` 대신 `UserMessage`로 저장하도록 `SummarizingChatMemory`를 수정해서 해결했습니다. 요약 판별 조건(`isSummaryMessage`)과 프론트(`index.html`)의 SUMMARY 카드 판별 조건도 함께 `USER` 타입 기준으로 맞췄습니다.
- 오래된 메시지를 자르는 `cutIndex`가 홀수면 user/assistant 페어가 어긋날 수 있어서, 짝수로 내림 처리하는 보정을 추가했습니다.
- 요약도 LLM 호출이기 때문에 대화가 길어질수록 비용/지연 시간이 늘어납니다. 실무라면 요약 주기를 더 길게 잡거나 더 저렴한 모델로 요약만 따로 돌리는 것도 고려해볼 만합니다.
