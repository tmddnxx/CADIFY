package com.cadify.cadifyWAS.service.admin;

import com.cadify.cadifyWAS.model.dto.admin.member.AdminMemberDTO;
import com.cadify.cadifyWAS.model.dto.admin.member.FilteredMemberResponse;
import com.cadify.cadifyWAS.model.dto.admin.member.PersonalOrderResponse;
import com.cadify.cadifyWAS.repository.OrderRepository;
import com.cadify.cadifyWAS.repository.admin.member.AdminMemberQueryRepository;
import com.cadify.cadifyWAS.repository.member.OAuthMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class AdminMemberService {

    private final AdminMemberQueryRepository adminMemberRepository;
    // 조건별 회원 조회
    public List<FilteredMemberResponse> getFilteredMembers(AdminMemberDTO.FilteredMemberRequest request){
        return adminMemberRepository.findFilteredMembersData(request);
    }
    // 회원별 주문 조회
    public List<PersonalOrderResponse> getPersonalOrderList(AdminMemberDTO.PersonalOrderRequest request){
        return adminMemberRepository.findPersonalOrderList(request.getEmail());
    }
}
