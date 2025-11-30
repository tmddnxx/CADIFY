package com.cadify.cadifyWAS.controller.admin;

import com.cadify.cadifyWAS.model.dto.admin.dashboard.*;
import com.cadify.cadifyWAS.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
@Log4j2
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    // 총 회원, 금주 신규 회원  카드
    @GetMapping("/user")
    public ResponseEntity<MemberCardResponse> getMemberCardData(){
        MemberCardResponse response = adminDashboardService.findMemberCardData();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    // 총 주문수, 오늘 납기  카드
    @GetMapping("/order")
    public ResponseEntity<OrderCardResponse> getOrderCardData(){
        OrderCardResponse response = adminDashboardService.findOrderCardData();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    // 일, 주, 월 별 매출  카드
    @GetMapping("/revenue")
    public ResponseEntity<RevenueCardResponse> getRevenueCardData(){
        RevenueCardResponse response = adminDashboardService.findRevenueCardData();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    // 월간 매출 추이 차트
    @GetMapping("/chart/revenue")
    public ResponseEntity<List<MonthlyRevenueResponse>> getMonthlyRevenueChartData(){
        List<MonthlyRevenueResponse> response = adminDashboardService.findMonthlyRevenueChartData();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    // 고객 주문금액 순위 차트
    @GetMapping("/chart/topMember")
    public ResponseEntity<List<TopRevenueMemberResponse>> getTopRevenueMembers() {
        List<TopRevenueMemberResponse> response = adminDashboardService.getTopRevenueMembers();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    // 가공 유형 비율 차트
    @GetMapping("/chart/processingType")
    public ResponseEntity<PreferredProcessingTypeResponse> getPreferredProcessingType(){
        PreferredProcessingTypeResponse response = adminDashboardService.getPreferredProcessingTypeValues();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
