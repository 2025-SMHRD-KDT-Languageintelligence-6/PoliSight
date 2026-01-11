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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/join", "/css/**", "/js/**", "/images/**").permitAll()
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