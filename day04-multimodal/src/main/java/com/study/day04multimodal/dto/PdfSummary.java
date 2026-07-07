package com.study.day04multimodal.dto;

import java.util.List;

public record PdfSummary(
        String summary,
        List<String> keyPoint
) {
}
