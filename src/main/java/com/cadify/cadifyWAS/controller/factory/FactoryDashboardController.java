package com.cadify.cadifyWAS.controller.factory;

import com.cadify.cadifyWAS.model.dto.admin.dashboard.MonthlyRevenueResponse;
import com.cadify.cadifyWAS.model.dto.admin.order.SettlementCardsResponse;
import com.cadify.cadifyWAS.model.dto.factory.dashboard.DashboardCardResponse;
import com.cadify.cadifyWAS.model.dto.factory.dashboard.WeeklyOrderCountResponse;
import com.cadify.cadifyWAS.service.factory.FactoryDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/factory/dashboard")
public class FactoryDashboardController {

    private final FactoryDashboardService dashboardService;

    // 대시보드 주문 관련 카드 데이터 조회
    @GetMapping("/card")
    public ResponseEntity<DashboardCardResponse> getDashboardCards(){
        DashboardCardResponse response = dashboardService.getCardData();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    // 대시보드 월별 매출 차트 데이터
    @GetMapping("chart/revenue/month")
    public ResponseEntity<List<MonthlyRevenueResponse>> getMonthlyRevenueChart(){
        List<MonthlyRevenueResponse> response = dashboardService.getMonthlyRevenueChartData();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    // 대시보드 이번주 요일별 주문 수 차트
    @GetMapping("/chart/orderCount")
    public ResponseEntity<List<WeeklyOrderCountResponse>> getWeeklyOrderCount(){
        List<WeeklyOrderCountResponse> response = dashboardService.getWeeklyOrderCount();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    // 정산관리 카드데이터
    @GetMapping("/card/settlement")
    public ResponseEntity<SettlementCardsResponse> getSettlementCardData(){
        SettlementCardsResponse response = dashboardService.getSettlementCardData();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
