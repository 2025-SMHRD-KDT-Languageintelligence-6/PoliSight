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
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        // [수정] "/setup"을 포함하여 비회원 접근 허용 경로 설정
                        .requestMatchers(
                                "/", "/login", "/join",           // 1. 기본 진입점
                                "/css/**", "/js/**", "/images/**", // 2. 정적 리소스
                                "/mail/**",  // 3. 이메일 인증 관련
                                "/user/**",  // 4. 비밀번호 찾기 등 회원 관련 API

                                // 5. 서비스 핵심 페이지 (비회원 체험용)
                                "/policy",
                                "/simulation",
                                "/setup"      // ★ [추가됨] 조건 설정 페이지 허용
                        ).permitAll()

                        // 그 외 모든 요청은 로그인 필요
                        .anyRequest().authenticated()
                )
                // 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserCustomService)
                        )
                        .successHandler(successHandler())
                );

        return http.build();
    }

    // 소셜 로그인 성공 시 실행되는 핸들러 (세션 처리 등)
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            String email = authentication.getName();

            // DB에서 회원 정보 조회
            MemberDto member = memberMapper.selectMemberByEmail(email);

            // 세션에 로그인 회원 정보 저장 (키: loginMember)
            HttpSession session = request.getSession();
            session.setAttribute("loginMember", member);

            // 메인 페이지로 리다이렉트
            response.sendRedirect("/");
        };
    }
}