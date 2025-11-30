package com.cadify.cadifyWAS.controller.member;

import com.cadify.cadifyWAS.model.dto.member.agreement.MemberAgreementDTO;
import com.cadify.cadifyWAS.model.dto.member.agreement.MemberAgreementResponse;
import com.cadify.cadifyWAS.service.member.MemberAgreementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member/agreement")
public class MemberAgreementController {

    private final MemberAgreementService agreementService;

    // 동의 프로세스
    @PostMapping
    public ResponseEntity<Void> registerAgreement(@RequestBody List<MemberAgreementDTO.AgreementRequest> request){
        agreementService.agreementProcess(request);
        return ResponseEntity.ok().build();
    }

    // 사용자별 필수 동의여부 조회
    @GetMapping("/required")
    public ResponseEntity<List<MemberAgreementResponse>> getPersonalRequiredAgreedList(){
        List<MemberAgreementResponse> response = agreementService.getPersonalRequiredAgreedList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 사용자별 마케팅 동의여부 조회
    @GetMapping("/marketing")
    public ResponseEntity<List<MemberAgreementResponse>> getPersonalMarketingAgreedList(){
        List<MemberAgreementResponse> response = agreementService.getPersonalMarketingAgreedList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
