package com.simpol.polisight.config;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.mapper.MemberMapper;
import com.simpol.polisight.service.OAuth2UserCustomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final MemberMapper memberMapper;

    public SecurityConfig(OAuth2UserCustomService oAuth2UserCustomService, MemberMapper memberMapper) {
        this.oAuth2UserCustomService = oAuth2UserCustomService;
        this.memberMapper = memberMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (개발 중 편의를 위해)
                .authorizeHttpRequests(auth -> auth
                        // [핵심] 와일드카드(**)를 사용해서 그룹으로 허용합니다.
                        .requestMatchers(
                                "/", "/login", "/join",           // 1. 기본 로그인/가입
                                "/css/**", "/js/**", "/images/**", // 2. 정적 파일 전체

                                "/mail/**",  // ★ 메일 관련 모든 주소 프리패스 (send, verify, send-reset 등)
                                "/user/**",  // ★ 비번 찾기 관련 모든 주소 프리패스 (reset-pw, update-pw 등)

                                "/policy", "/simulation" // ★ 정책/시뮬레이션 페이지
                        ).permitAll()

                        // 나머지는 로그인해야 접근 가능 (마이페이지 등)
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserCustomService)
                        )
                        .successHandler(successHandler())
                );
        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            String email = authentication.getName();

            // DB 조회
            MemberDto member = memberMapper.selectMemberByEmail(email);

            // 세션 저장 (loginMember)
            HttpSession session = request.getSession();
            session.setAttribute("loginMember", member);

            response.sendRedirect("/");
        };
    }
}