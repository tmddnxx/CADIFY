package com.cadify.cadifyWAS.service.admin;

import com.cadify.cadifyWAS.model.dto.admin.dashboard.*;
import com.cadify.cadifyWAS.repository.admin.member.AdminMemberQueryRepository;
import com.cadify.cadifyWAS.repository.admin.order.AdminOrderQueryRepository;
import com.cadify.cadifyWAS.repository.admin.orderItem.AdminOrderItemQueryRepository;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Log4j2
public class AdminDashboardService {

    private final AdminMemberQueryRepository adminMemberRepository;
    private final AdminOrderQueryRepository adminOrderRepository;
    private final AdminOrderItemQueryRepository adminOrderItemRepository;

    // 회원 관련 카드 데이터
    @Transactional(readOnly = true)
    public MemberCardResponse findMemberCardData() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();

        return adminMemberRepository.findMemberCardStatus(today, startOfWeek);
    }

    // 주문 관련 카드 데이터
    @Transactional(readOnly = true)
    public OrderCardResponse findOrderCardData() {
        LocalDate today = LocalDate.now();

        return adminOrderRepository.findOrderCardStatus(today);
    }

    // 매출 관련 카드 데이터
    @Transactional(readOnly = true)
    public RevenueCardResponse findRevenueCardData() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime thisWeek = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        LocalDateTime thisMonth = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay();

        Tuple tuple = adminOrderRepository.findRevenueCardStatus(today, thisWeek, thisMonth);

        RevenueCardTemp todayRevenue = new RevenueCardTemp(tuple.get(0, Integer.class), tuple.get(1, Integer.class));
        RevenueCardTemp thisWeekRevenue = new RevenueCardTemp(tuple.get(2, Integer.class), tuple.get(3, Integer.class));
        RevenueCardTemp thisMonthRevenue = new RevenueCardTemp(tuple.get(4, Integer.class), tuple.get(5, Integer.class));

        return new RevenueCardResponse(todayRevenue, thisWeekRevenue, thisMonthRevenue);
    }

    // 월별 매출 차트 데이터
    @Transactional(readOnly = true)
    public List<MonthlyRevenueResponse> findMonthlyRevenueChartData() {
        // 지난 12개월 이전 시점
        LocalDateTime twelveMonthAgo = LocalDate.now().minusMonths(11).withDayOfMonth(1).atStartOfDay();
        // 지난 12개월을 기준으로, 매출액을 담을 12개의 Integer List
        List<Integer> last12Month = IntStream.range(0, 12)
                .mapToObj(i -> twelveMonthAgo.toLocalDate().plusMonths(i).getMonthValue())
                .toList();
        //
        List<Tuple> results = adminOrderRepository.findMonthlyRevenueChartStatus(twelveMonthAgo);
        List<MonthlyRevenueResponse> response = new ArrayList<>();

        Map<Integer, Integer> revenueMap = results.stream()
                .collect(Collectors.toMap(
                        t -> t.get(1, Integer.class),
                        t -> Optional.ofNullable(t.get(2, Integer.class)).orElse(0)
                ));

        for(Integer monthValue : last12Month){
            Integer revenue = revenueMap.getOrDefault(monthValue, 0);

            response.add(new MonthlyRevenueResponse(monthValue, revenue));
        }

        return response;
    }

    // 고객별 매출 금액 순위
    @Transactional(readOnly = true)
    public List<TopRevenueMemberResponse> getTopRevenueMembers(){
        return adminMemberRepository.findTopRevenueMembers();
    }

    // 가공 유형별 주문 수
    @Transactional(readOnly = true)
    public PreferredProcessingTypeResponse getPreferredProcessingTypeValues(){
        PreferredProcessingTypeResponse results = adminOrderItemRepository.findPreferredProcessingType();
        // 기본값 리턴
        if(results == null){
            return new PreferredProcessingTypeResponse(0, 0);
        }
        return adminOrderItemRepository.findPreferredProcessingType();
    }
}
