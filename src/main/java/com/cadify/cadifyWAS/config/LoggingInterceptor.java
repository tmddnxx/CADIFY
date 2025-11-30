package com.cadify.cadifyWAS.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Log4j2
public class LoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String uri = request.getRequestURI();

        if (uri.equals("/health")) {
            return true;
        }

        if (handler instanceof HandlerMethod handlerMethod) {
            String methodName = handlerMethod.getMethod().getName();
            String className = handlerMethod.getBeanType().getSimpleName();
            String httpMethod = request.getMethod();
            String requestTime = java.time.LocalDateTime.now().toString();

            log.info("""
                    \n🟢 [API REQUEST START]
                    ▶️ TIME       : {}
                    ▶️ URI        : {}
                    ▶️ HTTP METHOD: {}
                    ▶️ CONTROLLER : {}.{}()
                    """, requestTime ,uri, httpMethod, className, methodName);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {

        String uri = request.getRequestURI();

        // /health 경로에서 에러가 발생했을 때만 로그
        if ("/health".equals(uri)) {
            if (response.getStatus() >= 400 || ex != null) {
                log.error("🔴 [HEALTH CHECK FAILED] Status: {}, Error: {}",
                        response.getStatus(), ex != null ? ex.getMessage() : "HTTP Error");
            }
        }
    }
}