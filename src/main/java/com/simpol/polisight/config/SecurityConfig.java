package com.simpol.polisight.config;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.mapper.MemberMapper;
import com.simpol.polisight.service.MemberService;
import com.simpol.polisight.service.OAuth2UserCustomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(OAuth2UserCustomService oAuth2UserCustomService,
                          MemberMapper memberMapper,
                          PasswordEncoder passwordEncoder) {
        this.oAuth2UserCustomService = oAuth2UserCustomService;
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       MemberService memberService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(memberService)
                .passwordEncoder(passwordEncoder);

        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ✅ [수정 포인트] 뒤에 /**를 붙여야 하위 경로까지 모두 허용됩니다.
                        .requestMatchers(
                                "/", "/login", "/join",
                                "/css/**", "/js/**", "/images/**", "/live2d/**",
                                "/mail/**", "/user/**",
                                "/policy/**",       // /policy 관련 모든 하위 경로 허용
                                "/simulation/**",   // ✅ /simulation/analyze 포함 모든 하위 경로 허용
                                "/setup"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("userPw")
                        .successHandler(successHandler())
                        .permitAll()
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
            HttpSession session = request.getSession();
            String email = authentication.getName();
            MemberDto member = memberMapper.selectMemberByEmail(email);

            // 1. 로그인 세션 처리 (기존 코드 유지)
            if (member != null) {
                session.setAttribute("loginMember", member);
            }

            // 2. [수정] 신규 유저 꼬리표 확인 (1단계에서 붙인 것)
            Boolean isNew = (Boolean) session.getAttribute("socialIsNew");

            if (isNew != null && isNew) {
                // ★ [신규 유저] -> 꼬리표 떼고, 로그인 페이지(모달)로 납치
                session.removeAttribute("socialIsNew");
                response.sendRedirect("/login?social_welcome=true");
            } else {
                // ★ [기존 유저] -> 원래 가던 대로 정책 페이지로 이동 (기존 유지)
                response.sendRedirect("/policy");
            }
        };
    }
}