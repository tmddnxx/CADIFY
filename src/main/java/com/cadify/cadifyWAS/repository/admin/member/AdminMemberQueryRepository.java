package com.cadify.cadifyWAS.repository.admin.member;

import com.cadify.cadifyWAS.model.dto.admin.dashboard.MemberCardResponse;
import com.cadify.cadifyWAS.model.dto.admin.dashboard.TopRevenueMemberResponse;
import com.cadify.cadifyWAS.model.dto.admin.member.AdminMemberDTO;
import com.cadify.cadifyWAS.model.dto.admin.member.FilteredMemberResponse;
import com.cadify.cadifyWAS.model.dto.admin.member.PersonalOrderResponse;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminMemberQueryRepository {

    // Admin Dashboard : 대시보드 회원 카드 데이터 조회
    MemberCardResponse findMemberCardStatus(LocalDateTime today, LocalDateTime thisWeek);

    // Admin User : 조건별 회원 조회
    List<FilteredMemberResponse> findFilteredMembersData(AdminMemberDTO.FilteredMemberRequest request);

    // Admin User : 회월별 주문 목록 조회
    List<PersonalOrderResponse> findPersonalOrderList(String email);

    // Admin Dashboard : 최고 매출 회원 차트
    List<TopRevenueMemberResponse> findTopRevenueMembers();
}
