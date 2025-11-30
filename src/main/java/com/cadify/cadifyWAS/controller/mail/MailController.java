package com.cadify.cadifyWAS.controller.mail;

import com.cadify.cadifyWAS.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail")
public class MailController {

    private final MailService mailService;

    @GetMapping("/test")
    public void sendSimpleMailMessage(){
        mailService.sendMail("wprhks536@gmail.com", null, null);
    }

}
