package com.study.day02promptoutput.dto;

import java.util.List;

// 여행 하루치 일정을 담는 record
public record DayPlan(
        int day,
        List<String> activities
) {
}
