package com.study.day04multimodal.dto;

import java.util.List;

/** 화이트보드 사진 분석 결과. ReceiptInfo와 같은 패턴 — .entity()로 바로 매핑. */
public record MeetingBoard(
        List<String> topics,        // 화이트보드에 적힌 주제들
        List<String> actionItems    // 액션 아이템 (담당자가 적혀있으면 그대로 포함)
) {
}
