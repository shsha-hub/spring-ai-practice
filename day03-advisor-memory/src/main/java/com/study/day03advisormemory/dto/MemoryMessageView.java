package com.study.day03advisormemory.dto;

/**
 * /api/chat-summary/history 응답 형태.
 * 저장소에 실제로 남아있는 메시지 하나를 표현한다.
 * type이 SYSTEM이면서 텍스트가 "[이전 대화 요약]"으로 시작하면 요약 메시지다.
 */
public record MemoryMessageView(String type, String text) {
}
