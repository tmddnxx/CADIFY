package com.cadify.cadifyWAS.controller.admin;

import com.cadify.cadifyWAS.model.dto.admin.member.AdminMemberDTO;
import com.cadify.cadifyWAS.model.dto.admin.member.FilteredMemberResponse;
import com.cadify.cadifyWAS.model.dto.admin.member.PersonalOrderResponse;
import com.cadify.cadifyWAS.service.admin.AdminMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // 회사이름, 주문수, 가입일, ( 이메일 or 회원이름 ), 정렬기준( 이름, 주문수, 가입일, 총 결제액 ) 회원 정보 조회
    @GetMapping("/filters")
    public ResponseEntity<List<FilteredMemberResponse>> getMemberInfoByName(@ModelAttribute AdminMemberDTO.FilteredMemberRequest request){

        log.info(request.getSearch() + request.getCompanyName() + request.getJoined());

        List<FilteredMemberResponse> response = adminMemberService.getFilteredMembers(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회원별 주문내역 조회
    @PostMapping("/orders")
    public ResponseEntity<List<PersonalOrderResponse>> getPersonalOrderList(@Valid @RequestBody AdminMemberDTO.PersonalOrderRequest request){
        List<PersonalOrderResponse> response = adminMemberService.getPersonalOrderList(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
