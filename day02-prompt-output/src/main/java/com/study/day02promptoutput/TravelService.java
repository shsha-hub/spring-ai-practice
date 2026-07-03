package com.study.day02promptoutput;

import com.study.day02promptoutput.dto.DayPlan;
import com.study.day02promptoutput.dto.TravelPlanResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelService {

    // 준비물만 뽑을 때 쓰는 템플릿 (ListOutputConverter용)
    private static final String PACKING_TEMPLATE = """
            {destination}으로 {days}일 여행 갈 때 챙길 준비물 목록을 만드세요.
            """;

    // 일정만 뽑을 때 쓰는 템플릿 (ParameterizedTypeReference용)
    private static final String ITINERARY_TEMPLATE = """
            {destination}으로 {days}일 여행을 갈 때의 일정을 하루 단위로 계획하세요.
            각 날짜(day)마다 2~3개의 activities를 포함하세요.
            """;

    // 준비물 + 일정을 한 번에 뽑을 때 쓰는 템플릿 (BeanOutputConverter용)
    // estimatedBudgetKr에 few-shot 개념 적용
    private static final String TRAVEL_PLAN_TEMPLATE = """
            {destination}으로 {days}일 여행을 계획하려 합니다.

            - packingList: 챙겨야 할 준비물 목록. 현실적으로 준비가 가능한 목록으로 만드세요.
            - itinerary: day(1부터 시작하는 정수)와 activities(2~3개)로 구성된 하루 단위 일정
            - estimatedBudgetKrw: 1인 기준 예상 총 경비(원화, 정수). 왕복 항공/교통비는 제외하고
              현지 숙박, 식비, 입장료, 현지 이동 비용만 합산하세요.
              예시: 제주도 2박 3일 기준 300000~500000원, 방콕 3박 4일 기준 400000~700000원 수준.
            - budgetNote: estimatedBudgetKrw에 무엇이 포함/제외됐는지 한 문장으로 설명

            여행지의 날씨와 특성, 물가 수준을 고려해서 만들어주세요.
            """;

    private final ChatClient chatClient;

    public TravelService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // 문자열 목록만 필요할 때 — ListOutputConverter
    public List<String> packingList(String destination, int days) {
        return chatClient.prompt()
                .user(u -> u.text(PACKING_TEMPLATE)
                        .param("destination", destination)
                        .param("days", days))
                .call()
                .entity(new ListOutputConverter(new DefaultConversionService()));
    }

    // 객체 여러 건이 필요할 때 — ParameterizedTypeReference
    public List<DayPlan> itinerary(String destination, int days) {
        return chatClient.prompt()
                .user(u -> u.text(ITINERARY_TEMPLATE)
                        .param("destination", destination)
                        .param("days", days))
                .call()
                .entity(new ParameterizedTypeReference<List<DayPlan>>() {});
    }

    // 준비물 + 일정을 한 번에 — 중첩 record를 BeanOutputConverter로 한 번에 매핑
    public TravelPlanResponse travelPlan(String destination, int days) {
        return chatClient.prompt()
                .user(u -> u.text(TRAVEL_PLAN_TEMPLATE)
                        .param("destination", destination)
                        .param("days", days))
                .call()
                .entity(TravelPlanResponse.class);
    }
}
