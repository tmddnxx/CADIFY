package com.cadify.cadifyWAS.controller.auth;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ErrorResponse;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.auth.AuthDTO;
import com.cadify.cadifyWAS.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody AuthDTO.RefreshTokenInfo request) {
        // DB에서 Toekn 테이블 레코드 삭제
        authService.logoutProcess(request);
        // http only refresh token 삭제 -> 만료시간 0 세팅
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)              // 쿠키 즉시 만료
                .sameSite("None")       // sameSite 설정 로그인 시 제공되는 쿠키와 동일
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body("Logout Success");
    }

    // refresh 토큰 검증 및 access 토큰 발급
    @PostMapping("/refresh")
    public ResponseEntity<?> accessTokenRequest(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        // 쿠키가 존재하지 않을 경우 -> 로그인 필요
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of(new CustomLogicException(ExceptionCode.REQUIRED_LOGIN)));
        }
        // 쿠키가 존재할 경우 -> refreshToken String 추출
        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }
        // refresh 토큰이 null 일 경우
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of(new CustomLogicException(ExceptionCode.REQUIRED_LOGIN)));
        }
        // 새 Access Token 발급
        String newAccessToken = authService.refreshProcess(refreshToken);

        // 헤더에 새 토큰 추가
        response.setHeader("Authorization", "Bearer " + newAccessToken);

        // 본문 없이 응답, 204 반환
        return ResponseEntity.noContent().build();
    }

}