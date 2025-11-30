package com.cadify.cadifyWAS.config;

import com.cadify.cadifyWAS.security.exception.CustomAuthenticationFailureHandler;
import com.cadify.cadifyWAS.security.common.CustomAuthenticationSuccessHandler;
import com.cadify.cadifyWAS.security.exception.CustomAccessDeniedHandler;
import com.cadify.cadifyWAS.security.exception.CustomAuthenticationEntryPoint;
import com.cadify.cadifyWAS.security.form.CustomUserDetailsService;
import com.cadify.cadifyWAS.security.jwt.JwtAuthenticationFilter;
import com.cadify.cadifyWAS.security.oAuth.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration // Spring 설정 클래스
@EnableWebSecurity(debug = false) // Spring Security 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    // OAuth 인증 방식
    private final CustomOAuth2UserService customOAuth2UserService;
    // Form 인증 방식
    private final CustomUserDetailsService customUserDetailsService;

    // AuthenticaionpProvider 등록 : Form 로그인용, ( UserDetailsService + PasswordEncoder )
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    // AuthenticationManager 등록 ( daoAuthenticationProvider 사용 )
    @Bean
    public AuthenticationManager authenticationManger(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(daoAuthenticationProvider());
        return builder.build();
    }

    // PasswordEncoder Bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // HTTP 보안 설정 및 JWT 필터 추가
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        // cors 설정
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // csrf ( 보호 비활성화 )
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(
                        headerConfig -> headerConfig.frameOptions(
                                HeadersConfigurer.FrameOptionsConfig::deny // iframe ( 차단 )
                        )
                );

        // session ( 사용X 비활성화 )
        http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // 접근 권한
        http
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(
                                        "/oauth2/authorization/google",
                                        "/login/oauth2/code/google",
                                        "/api/auth/public/**",
                                        "/swagger-ui/**",
                                        "/api/auth/refresh"
//                                        "/favicon.ico",
//                                        "/login?error",
//                                        "/error",
                                ).permitAll()
//                                .anyRequest().authenticated()
                                .anyRequest().permitAll()
                );

        // jwt token
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // From Login
        http
                .formLogin(form -> form
                        .loginProcessingUrl("/api/factory/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                );

        // OAuth2 Login
        http
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                );

        // exception handling
        http
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()) // 인증 오류 처리
                        .accessDeniedHandler(new CustomAccessDeniedHandler()) // 접근 거부 처리
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        // cors 설정 ( 기본 )
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "https://cadify.kr",    // 서비스 도메인
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // OPTIONS: 사전 요청
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true); // 쿠키 허용

        // 패턴 별 cors 설정 적용 ( 전체 패턴 적용 중 )
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}