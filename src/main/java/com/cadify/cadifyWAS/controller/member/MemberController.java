package com.cadify.cadifyWAS.controller.member;

import com.cadify.cadifyWAS.model.dto.auth.AuthDTO;
import com.cadify.cadifyWAS.model.dto.member.MemberDTO;
import com.cadify.cadifyWAS.service.member.OAuthMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {

    private final OAuthMemberService oAuthMemberService;

    // 로그인 이후 기본 회원 정보
    @GetMapping("/me")
    public ResponseEntity<MemberDTO.MemberInfo> getMemberInfo(){
        log.info("member me 호출");
        MemberDTO.MemberInfo memberInfo = oAuthMemberService.getMemberInfo();
        return new ResponseEntity<>(memberInfo, HttpStatus.OK);
    }

    // 첫 OAuth2 로그인 이후 (사용자 유형 선택: MemberRole)
    @PatchMapping("/assign/role")
    public ResponseEntity<MemberDTO.MemberInfo> assignMemberRoleForFirstLogin(@Valid @RequestBody MemberDTO.AssignRoleRequest request){
        AuthDTO.AssignRoleResult result = oAuthMemberService.assignMemberRole(request);

        MemberDTO.MemberInfo response = result.getMemberInfo();

        log.info("After assign/role : new Token : {}", result.getNewAccessToken());
        log.info("After assign/role : new Role : {}", response.getRole());

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.getNewAccessToken())
                .body(response);
    }

    // 인증번호 전송 요청
    @PostMapping("/auth/sms/phone")
    public ResponseEntity<AuthDTO.AuthSMSResponse> getSMSAuthCode(@Valid @RequestBody AuthDTO.PhoneAuthRequestDTO request){
        AuthDTO.AuthSMSResponse response = oAuthMemberService.getSmsAuthCode(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 인증번호 검증 요청
    @PostMapping("/auth/sms/code")
    public ResponseEntity<Void> verifySMSAuthCode(@Valid @RequestBody AuthDTO.VerifyAuthCodeRequestDTO request){
        oAuthMemberService.verifySMSAuthCode(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 회원 정보 수정
    @PatchMapping
    public ResponseEntity<MemberDTO.MemberInfo> updateMemberInfo(@Valid @RequestBody MemberDTO.UpdateMember request){
        MemberDTO.MemberInfo response = oAuthMemberService.updateMemberInfo(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
