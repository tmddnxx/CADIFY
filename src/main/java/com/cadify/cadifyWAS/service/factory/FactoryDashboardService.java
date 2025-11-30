package com.cadify.cadifyWAS.service.factory;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.admin.dashboard.MonthlyRevenueResponse;
import com.cadify.cadifyWAS.model.dto.admin.order.SettlementCardsResponse;
import com.cadify.cadifyWAS.model.dto.factory.dashboard.DashboardCardResponse;
import com.cadify.cadifyWAS.model.dto.factory.dashboard.WeeklyOrderCountResponse;
import com.cadify.cadifyWAS.model.entity.factory.Factory;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.repository.factory.admin.FactoryAdminRepository;
import com.cadify.cadifyWAS.repository.factory.order.FactoryOrderQueryRepository;
import com.cadify.cadifyWAS.security.jwt.JwtPrincipal;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryDashboardService {
    private final FactoryOrderQueryRepository factoryOrderRepository;
    private final FactoryAdminRepository factoryAdminRepository;
    private final JwtUtil jwtUtil;

    // 공장관리 대시보드 카드 데이터 ( 총 주문, 제작 중, 완료된 주문, 이번주 들어온 주문, 일, 주, 달 매출 )
    public DashboardCardResponse getCardData() {
        // 공장 관리자 소속 FactoryKey 리턴
        Factory factory = getValidFactory();
        return factoryOrderRepository.getDashboardCardData(factory.getFactoryKey(), factory.getFactoryType());
    }

    // 공장 대시보드 월 매출 차트 데이터 (지난 12개월)
    public List<MonthlyRevenueResponse> getMonthlyRevenueChartData() {
        Factory factory = getValidFactory();
        // 현재 기준 12개월 전 시점
        LocalDateTime twelveMonthAgo = LocalDate.now().minusMonths(11).withDayOfMonth(1).atStartOfDay();
        // 12개월 List (Integer 형 월 표현)
        List<Integer> last12Month = IntStream.range(0, 12)
                .mapToObj(i -> twelveMonthAgo.plusMonths(i).getMonthValue())
                .toList();
        // query 결과 & map 으로 가공
        List<Tuple> results = factoryOrderRepository
                .getMonthlyRevenueData(factory.getFactoryKey(), factory.getFactoryType(), twelveMonthAgo);
        Map<Integer, Integer> revenueMap = results.stream()
                .collect(Collectors.toMap(
                        t -> t.get(1, Integer.class),
                        t -> Optional.ofNullable(t.get(2, Integer.class)).orElse(0)
                ));
        // response 객체 생성
        List<MonthlyRevenueResponse> response = new ArrayList<>();
        for(Integer month : last12Month){
            Integer revenue = revenueMap.getOrDefault(month, 0);
            response.add(new MonthlyRevenueResponse(month, revenue));
        }

        return response;
    }
    // 요일별 주문 수 조회
    @Transactional(readOnly = true)
    public List<WeeklyOrderCountResponse> getWeeklyOrderCount(){
        Factory factory = getValidFactory();
        String[] korDayOfWeek = {"월", "화", "수", "목", "금", "토", "일"};
        // 오늘 기준 1주일 전
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
        // 오늘 기준 한주간 요일 리스트
        List<LocalDate> dateList = IntStream.range(0, 7)
                .mapToObj(i -> sevenDaysAgo.toLocalDate().plusDays(i))
                .toList();
        List<Tuple> result = factoryOrderRepository
                .getWeeklyOrderCount(factory.getFactoryKey(), factory.getFactoryType(), sevenDaysAgo);
        Map<LocalDate, Long> countMap = result.stream()
                .collect(Collectors.toMap(
                        t -> t.get(0, java.sql.Date.class).toLocalDate(),
                        t -> Optional.ofNullable(t.get(1, Long.class)).orElse(0L)
                ));

        // 응답 객체
        List<WeeklyOrderCountResponse> response = new ArrayList<>();
        for(LocalDate date : dateList){
            Long count = countMap.getOrDefault(date, 0L);
            int dayOfWeek = date.getDayOfWeek().getValue();
            response.add(new WeeklyOrderCountResponse(korDayOfWeek[dayOfWeek-1], count));
        }

        return response;
    }

    // 정산 카드 데이터
    @Transactional(readOnly = true)
    public SettlementCardsResponse getSettlementCardData(){
        Factory factory = getValidFactory();
        return factoryOrderRepository.getSettlementCardsData(factory.getFactoryKey(), factory.getFactoryType());
    }


//  --------------------------------- 클래스 내부 유틸

    // 인증객체를 사용해서 FactoryType 조회
    private Factory getValidFactory() {
        JwtPrincipal principal = jwtUtil.getAuthPrincipalObject();
        log.info("getValidFactory() -> memberKey : " + principal.getMemberKey());
        // 인증객체의 역할이 Factory 인지 확인
        if (principal.getRole() != MemberRole.FACTORY && principal.getRole() != MemberRole.ADMIN) {
            throw new CustomLogicException(ExceptionCode.NOT_FACTORY_ADMIN);
        }
        // 공장관리자가 소속된 Factory 의 가공가능 유형 리턴
        return factoryAdminRepository.findFactoryByMemberKey(principal.getMemberKey());
    }
}
