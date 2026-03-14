package com.cadify.cadifyWAS.service.admin;

import com.cadify.cadifyWAS.model.dto.admin.member.AdminMemberDTO;
import com.cadify.cadifyWAS.model.dto.admin.member.FilteredMemberResponse;
import com.cadify.cadifyWAS.model.dto.admin.member.PersonalOrderResponse;
import com.cadify.cadifyWAS.repository.OrderRepository;
import com.cadify.cadifyWAS.repository.admin.member.AdminMemberQueryRepository;
import com.cadify.cadifyWAS.repository.member.OAuthMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMemberService {

    private final AdminMemberQueryRepository adminMemberRepository;
    // 조건별 회원 조회
    @Transactional(readOnly = true)
    public List<FilteredMemberResponse> getFilteredMembers(AdminMemberDTO.FilteredMemberRequest request){
        return adminMemberRepository.findFilteredMembersData(request);
    }
    // 회원별 주문 조회
    @Transactional(readOnly = true)
    public List<PersonalOrderResponse> getPersonalOrderList(AdminMemberDTO.PersonalOrderRequest request){
        return adminMemberRepository.findPersonalOrderList(request.getEmail());
    }
}
