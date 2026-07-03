package com.study.day02promptoutput;

import com.study.day02promptoutput.dto.DayPlan;
import com.study.day02promptoutput.dto.TravelPlanResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TravelController {

    private final TravelService travelService;

    public TravelController(TravelService travelService) {
        this.travelService = travelService;
    }

    // 준비물 목록만 — List<String>
    @GetMapping("/api/travel/packing")
    public List<String> packing(@RequestParam String destination,
                                @RequestParam int days) {
        return travelService.packingList(destination, days);
    }

    // 일정만 — List<DayPlan>
    @GetMapping("/api/travel/itinerary")
    public List<DayPlan> itinerary(@RequestParam String destination,
                                   @RequestParam int days) {
        return travelService.itinerary(destination, days);
    }

    // 준비물 + 일정 한 번에 — TravelPlanResponse
    @GetMapping("/api/travel")
    public TravelPlanResponse travelPlan(@RequestParam String destination,
                                         @RequestParam int days) {
        return travelService.travelPlan(destination, days);
    }
    
}
