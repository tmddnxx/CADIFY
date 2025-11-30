package com.cadify.cadifyWAS.controller.admin;

import com.cadify.cadifyWAS.model.dto.admin.member.AdminMemberDTO;
import com.cadify.cadifyWAS.model.dto.admin.member.FilteredMemberResponse;
import com.cadify.cadifyWAS.model.dto.admin.member.PersonalOrderResponse;
import com.cadify.cadifyWAS.service.admin.AdminMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
@Log4j2
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
    public ResponseEntity<List<PersonalOrderResponse>> getPersonalOrderList(@RequestBody AdminMemberDTO.PersonalOrderRequest request){
        List<PersonalOrderResponse> response = adminMemberService.getPersonalOrderList(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
