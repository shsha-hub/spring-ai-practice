package com.study.day04multimodal.dto;

import java.util.Date;
import java.util.List;

public record ReceiptInfo(
        String vendor,
        String totalAmount,
        Date date,
        List<String> items
) {
}
