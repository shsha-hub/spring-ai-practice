package com.study.day04multimodal.dto;

/** 안건 PDF 요약 결과. PdfSummary와 같은 패턴 — .entity()로 바로 매핑. */
public record AgendaSummary(
        String mainPoints
) {
}
