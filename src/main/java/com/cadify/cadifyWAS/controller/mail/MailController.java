package com.cadify.cadifyWAS.controller.mail;

import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.cadify.cadifyWAS.result.ResultCode.SEND_MAIL_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
@Slf4j
public class MailController {

    private final MailService mailService;

    // 메일 발송 테스트
    @GetMapping("/test")
    public ResponseEntity<ResultResponse> sendSimpleMailMessage() {
        mailService.sendMail("wprhks536@gmail.com", null, null);
        return ResponseEntity.ok().body(ResultResponse.of(SEND_MAIL_SUCCESS));
    }

}
