package com.cadify.cadifyWAS.util.api;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/*
    use : Solapi, Redis
*/
@Component
public class MessageAPI {

    private final DefaultMessageService messageService;
    private final RedisTemplate<String, String> redisTemplate;
    private final long authCodeTTL;

    public MessageAPI(@Value("${solapi.key}")String key,
                      @Value("${solapi.secret}")String secret,
                      @Qualifier("stringTemplate") RedisTemplate<String, String> redisTemplate){
        String apiURL = "https://api.solapi.com";
        this.messageService = NurigoApp.INSTANCE.initialize(key, secret, apiURL);
        this.redisTemplate = redisTemplate;
        this.authCodeTTL = 300;
    }

    public String sendAuthCode(String phone){
        String authCode = generateAuthCode();

        // redis 저장
        redisTemplate.opsForValue().set("AUTH_CODE:" + phone, authCode, authCodeTTL, TimeUnit.SECONDS);

        Message message = new Message();
        message.setFrom("01035161976");
        message.setTo(phone);
        message.setText(
                "[CADIFY] 인증번호 [" + authCode + "]를 입력해주세요. "
                + "사칭/전화사기에 주의하세요."
        );

        try{
            // 메시지 전송
            messageService.send(message);
        }catch (NurigoMessageNotReceivedException exception) {
            // 발송에 실패한 메시지 목록 확인
            throw new CustomLogicException(ExceptionCode.FAILED_SEND_CODE);
        } catch (Exception exception) {
            exception.printStackTrace();
            // 예상되지 않은 에러
            throw new CustomLogicException(ExceptionCode.UNKNOWN_CODE_ERROR);
        }

        return authCode;
    }

    public boolean verifyAuthCode(String phone, String inputCode){
        String authCode = redisTemplate.opsForValue().get("AUTH_CODE:" + phone);
        return inputCode.equals(authCode);
    }

    private String generateAuthCode(){
        Random random = new Random();
        int authCode = random.nextInt(900000) + 1000000;

        return String.valueOf(authCode);
    }
}