package com.simpol.polisight.config;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.mapper.MemberMapper;
import com.simpol.polisight.service.MemberService;
import com.simpol.polisight.service.OAuth2UserCustomService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.filter.ForwardedHeaderFilter;

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
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
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
                                "/setup", "/image/**",
                                "/api/policy/**",
                                // ▼▼▼ [여기만 추가됨] 비회원도 접근 가능해야 하는 약관 페이지 ▼▼▼
                                "/terms", "/privacy", "/legal"
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

            if (member != null) {
                session.setAttribute("loginMember", member);
            }

            Boolean isNew = (Boolean) session.getAttribute("socialIsNew");

            // [중요] fetch(AJAX) 요청인지 확인하는 헤더
            String xRequestedWith = request.getHeader("X-Requested-With");
            boolean isAjax = "XMLHttpRequest".equals(xRequestedWith) ||
                    request.getHeader("Accept").contains("application/json");

            if (isAjax) {
                // [정석] 비동기(fetch) 요청이면 리다이렉트 대신 JSON 응답을 보냅니다.
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json;charset=UTF-8");

                String targetUrl = (isNew != null && isNew) ? "/login?social_welcome=true" : "/policy";
                response.getWriter().print("{\"status\":\"success\", \"redirectUrl\":\"" + targetUrl + "\"}");
                response.getWriter().flush();
            } else {
                // 일반 로그인(OAuth2 등 브라우저 직접 이동)일 때는 기존처럼 리다이렉트
                if (isNew != null && isNew) {
                    session.removeAttribute("socialIsNew");
                    response.sendRedirect("/login?social_welcome=true");
                } else {
                    response.sendRedirect("/policy");
                }
            }
        };
    }
}