package com.cadify.cadifyWAS.security.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.Instant;

// 인증 후 권한 없음 MemberRole 에 따른 접근 허용 판단
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException{
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"code\": \"ACCESS_DENIED\"," + "\n"
                        + "\"status\": \"403\"," + "\n"
                        + "\"message\": \"접근 권한이 없습니다.\"," + "\n"
                        + "\"occurredAt\": \"" + Instant.now() + "\"}");
    }
}
