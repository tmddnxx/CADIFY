package com.cadify.cadifyWAS.security.exception;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ErrorResponse;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 잘못된 로그인 정보
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    // JSON 변환기
    private final ObjectMapper objectMapper;

    public CustomAuthenticationFailureHandler(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        CustomLogicException customLogicException;

        if(exception instanceof BadCredentialsException){
            customLogicException = new CustomLogicException(ExceptionCode.WRONG_PASSWORD);
        }else {
            customLogicException = new CustomLogicException(ExceptionCode.UNKNOWN_LOGIN_ERROR);
        }

        ErrorResponse errorResponse = ErrorResponse.of(customLogicException);

        response.setStatus(errorResponse.getStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
