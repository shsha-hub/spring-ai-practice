package com.study.day02promptoutput.dto;

import java.util.List;

// 준비물 목록 + 일별 일정 + 예상 예산을 한 번에 담는 record
// List<DayPlan>처럼 record 안에 record 리스트를 중첩해도
// BeanOutputConverter가 스키마를 만들어 그대로 매핑해준다
public record TravelPlanResponse(
        List<String> packingList,
        List<DayPlan> itinerary,
        int estimatedBudgetKrw,
        String budgetNote
) {
}
