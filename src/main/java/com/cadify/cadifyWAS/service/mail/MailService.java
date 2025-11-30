package com.cadify.cadifyWAS.service.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    // 매일 보내기
    public void sendMail(String to, String subject, String content) {
//        try{
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(to);
//            message.setSubject("wjdkslx1986@gmail.com");
//            message.setText("SEX");
//            message.setFrom("admin@cadify.kr");
//        }catch (Exception e){
//            throw new CustomLogicException(ExceptionCode.)
//        }
        SimpleMailMessage message = new SimpleMailMessage();
        // 수신자
        message.setTo(to);
        // 메일 제목
        message.setSubject("Cadify 메일 전송 테스트");
        // 내용
        message.setText("SEX");
        message.setFrom("admin@cadify.kr");

        mailSender.send(message);
    }
}
