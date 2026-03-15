package com.cadify.cadifyWAS.service.mail;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    // 메일 보내기
    public void sendMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        // 수신자
        message.setTo(to);
        // 메일 제목
        message.setSubject(subject);
        // 내용
        message.setText(content);
        message.setFrom("admin@cadify.kr");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("메일 전송 실패. 수신자: {}, 오류: {}", to, e.getMessage());
            throw new CustomLogicException(ExceptionCode.UNKNOWN_EXCEPTION_OCCURED);
        }
    }
}
