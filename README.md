# Day5 MCP 실습 — fetch/sequential-thinking 서버 연결 + 도구 분리

Spring AI Day5 강의(Tool Calling / MCP)를 바탕으로, 기존 `filesystem` MCP 서버 하나만 붙어있던 프로젝트에 `fetch`, `sequential-thinking` 서버를 추가로 연결하고, 서버별로 도구를 분리·조합하는 실습을 진행한 기록입니다.

## 실습 목표

1. `fetch` MCP 서버를 새로 연결한다
2. 여러 MCP 서버(`filesystem`·`fetch`·`sequential-thinking`)의 도구를 서버 이름 기준으로 분리·재사용 가능하게 설계한다
3. 서버 간 도구 연쇄(웹에서 가져와서 파일로 저장)를 실제로 확인한다
4. 로컬에서 자주 겪는 MCP 관련 에러들을 직접 재현하고 원인을 파악한다

---

## 최종 구조

```
McpToolCatalog (서버 이름 → 도구 배열 매핑)
├─ tools("filesystem")           filesystem 서버 도구만
├─ tools("fetch")                fetch 서버 도구만
├─ tools("sequential-thinking")  sequential-thinking 서버 도구만
├─ allTools()                    3개 서버 도구 전부
└─ toolsExcept("sequential-thinking")   특정 서버만 제외하고 합치기

McpChatService
├─ chat(serverKey, question)     서버 하나만 지정해서 대화
├─ chatAll(question)             모든 MCP 도구를 넘겨서 대화
└─ chatChain(question)           sequential-thinking을 뺀 도구로 연쇄 작업 전용

AiController
├─ GET /api/mcp/{serverKey}      서버 하나만 사용
├─ GET /api/mcp-chat             전체 도구 사용
└─ GET /api/mcp-chain            연쇄 작업 전용 (sequential-thinking 제외)
```

---

## application.yaml — MCP 서버 연결

```yaml
spring:
  ai:
    mcp:
      client:
        request-timeout: 30s
        stdio:
          connections:
            filesystem:
              command: npx.cmd
              args:
                - "-y"
                - "@modelcontextprotocol/server-filesystem"
                - "${user.dir}/mcp-sandbox"
            fetch:
              command: uvx
              args:
                - "mcp-server-fetch"
            sequential-thinking:
              command: npx.cmd
              args:
                - "-y"
                - "@modelcontextprotocol/server-sequential-thinking"
```

> ⚠️ MCP 서버 하나라도 기동에 실패하면(npx/uvx 부재 등) 앱 전체가 뜨지 않는다. 실습 전 `npx --version`, `uvx --version`으로 미리 확인할 것.

---

## McpToolCatalog.java

서버가 스스로 보고하는 이름(`getServerInfo().name()`)은 yaml에 적은 커넥션 키와 정확히 일치하지 않는다. (`filesystem` → 실제로는 `secure-filesystem-server`, `fetch` → `mcp-fetch`, `sequential-thinking` → `sequential-thinking-server`) 그래서 `equals`가 아니라 `contains`로 느슨하게 매칭해야 한다.

```java
package com.study.day05toolmcp.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class McpToolCatalog {

    private static final List<String> SERVER_KEYS = List.of("filesystem", "fetch", "sequential-thinking");

    private final Map<String, ToolCallback[]> toolsByServer;
    private final ToolCallback[] allTools;

    public McpToolCatalog(List<McpSyncClient> mcpClients) {
        this.allTools = toolsFrom(mcpClients);
        this.toolsByServer = SERVER_KEYS.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> toolsFrom(named(mcpClients, key))
                ));
    }

    public ToolCallback[] tools(String serverKey) {
        return toolsByServer.getOrDefault(serverKey, new ToolCallback[0]);
    }

    public ToolCallback[] allTools() {
        return allTools;
    }

    // 특정 서버(들)만 제외하고 나머지 전부 합치기
    public ToolCallback[] toolsExcept(String... excludeKeys) {
        List<String> excluded = List.of(excludeKeys);
        return SERVER_KEYS.stream()
                .filter(key -> !excluded.contains(key))
                .flatMap(key -> Arrays.stream(tools(key)))
                .toArray(ToolCallback[]::new);
    }

    private List<McpSyncClient> named(List<McpSyncClient> clients, String serverName) {
        return clients.stream()
                .filter(client -> client.getServerInfo().name().contains(serverName))
                .toList();
    }

    private ToolCallback[] toolsFrom(List<McpSyncClient> clients) {
        return SyncMcpToolCallbackProvider.builder()
                .mcpClients(clients)
                .build()
                .getToolCallbacks();
    }
}
```

---

## McpChatService.java

```java
package com.study.day05toolmcp.service;

import com.study.day05toolmcp.mcp.McpToolCatalog;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class McpChatService {

    private final ChatClient chatClient;
    private final McpToolCatalog catalog;

    public McpChatService(ChatClient.Builder builder, McpToolCatalog catalog) {
        this.chatClient = builder.build();
        this.catalog = catalog;
    }

    public String chat(String serverKey, String question) {
        return chatClient.prompt()
                .user(question)
                .tools((Object[]) catalog.tools(serverKey))
                .call()
                .content();
    }

    public String chatAll(String question) {
        return chatClient.prompt()
                .user(question)
                .tools((Object[]) catalog.allTools())
                .call()
                .content();
    }

    // sequential-thinking을 제외한 도구로 연쇄 작업 (fetch → filesystem 저장 등)
    public String chatChain(String question) {
        return chatClient.prompt()
                .user(question)
                .tools((Object[]) catalog.toolsExcept("sequential-thinking"))
                .call()
                .content();
    }
}
```

---

## AiController.java (MCP 관련 부분)

```java
@GetMapping("/api/mcp/{serverKey}")
public String mcp(@PathVariable String serverKey, @RequestParam String question) {
    return mcpChatService.chat(serverKey, question);
}

@GetMapping("/api/mcp-chat")
public String mcpChat(@RequestParam String question) {
    return mcpChatService.chatAll(question);
}

@GetMapping("/api/mcp-chain")
public String mcpChain(@RequestParam String question) {
    return mcpChatService.chatChain(question);
}
```

---

## 테스트 예시

```
# 서버 하나씩 (분리 검증)
GET /api/mcp/filesystem?question=현재 접근 가능한 디렉토리의 파일 목록을 보여줘
GET /api/mcp/fetch?question=https://httpbin.org/uuid 에 접속해서 나온 uuid 값을 알려줘
GET /api/mcp/sequential-thinking?question=피자를 만드는 과정을 3단계로 나눠서 생각해봐

# 도구 연쇄 (fetch로 가져와서 filesystem에 저장)
GET /api/mcp-chain?question=https://raw.githubusercontent.com/spring-projects/spring-ai/main/README.md 를 가져와서 readme.txt로 저장해줘
```

---

## 겪었던 에러와 원인

| 증상 | 원인 | 해결/교훈 |
|---|---|---|
| `filesystemTools()` 심볼 없음 | `McpToolCatalog`을 리팩터링하는 중 `McpChatService`가 옛 메서드를 여전히 참조 | 구조를 바꾸면 의존하는 다른 클래스도 함께 깨진다. 컴파일 에러 순서대로 하나씩 정리 |
| `named()`으로 필터링했더니 도구 개수 0개 | 서버가 스스로 보고하는 이름(`mcp-fetch`, `secure-filesystem-server`)이 yaml 커넥션 키(`fetch`, `filesystem`)와 정확히 일치하지 않음 | `equals` 대신 `contains`로 느슨하게 매칭 |
| `scandir '.../mcp-sandbox/mcp-sandbox'` 없음 | filesystem 서버는 yaml에 지정한 경로 자체를 이미 루트로 인식하는데, 질문에서 "mcp-sandbox 폴더 안"이라고 경로를 한 번 더 언급해서 중복 결합됨 | MCP 서버의 "루트 디렉토리" 개념을 이해하고, 질문에서는 상대경로/현재 디렉토리로만 지칭 |
| `nextThoughtNeeded` invalid input (boolean 없음) | `sequentialthinking` 도구는 스키마가 복잡한데, 경량 모델(`gemini-3.1-flash-lite`)이 필수 필드를 못 채우고 호출함. 질문을 단순화해도 도구가 `allTools()` 목록에 있는 한 모델이 자율적으로 골라 호출하다 재발 | 도구가 많고 스키마가 복잡할수록 모델이 헷갈린다 → 상황에 맞지 않는 도구는 아예 목록에서 제외 (`toolsExcept`) |
| `Failed to fetch robots.txt ... due to a connection issue` | `mcp-server-fetch`는 실제 콘텐츠를 가져오기 전에 대상 사이트의 `robots.txt`를 먼저 확인하는데, 그 요청 자체가 네트워크 문제로 실패 (`httpbin.org` 한정 이슈로 추정) | 다른 URL(GitHub raw 등)로 우회 확인. 근본 해결은 `mcp-server-fetch` 실행 인자에 `--ignore-robots-txt` 추가 (단, 실서비스에서는 신중히 사용) |

---

## 오늘 다시 익힌 핵심 개념

- **MCP Client 이름 vs 서버 자체 보고 이름**: yaml의 커넥션 키는 우리 앱이 관리하는 이름일 뿐, 서버가 `getServerInfo().name()`으로 스스로 리턴하는 이름과는 다를 수 있다.
- **도구 카탈로그는 시작 시 한 번만 계산**: `tools/list` 왕복은 비용이 있으므로, `@Component` 생성자에서 미리 분류해서 캐싱해두는 구조가 안전하다.
- **도구가 많을수록 모델이 헷갈린다**: 모든 도구를 항상 다 넘기기보다, 상황(단발성 조회 vs 연쇄 작업)에 맞는 도구 조합만 골라 넘기는 설계가 실전에서 더 안정적이다.
- **MCP 서버도 하나의 완전한 프로그램**: `robots.txt` 확인처럼 서버 내부적으로 처리하는 로직이 있고, 이것도 실패 지점이 될 수 있다.
