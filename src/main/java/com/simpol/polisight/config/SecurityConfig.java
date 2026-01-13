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
    private final PasswordEncoder passwordEncoder; // ★ 생성자 주입을 위해 추가

    // 생성자에 passwordEncoder를 추가하여 PasswordConfig에서 만든 빈을 가져옵니다.
    public SecurityConfig(OAuth2UserCustomService oAuth2UserCustomService,
                          MemberMapper memberMapper,
                          PasswordEncoder passwordEncoder) {
        this.oAuth2UserCustomService = oAuth2UserCustomService;
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // ★ 이미지에서 빨간 줄 떴던 부분: 인증 매니저 설정
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       MemberService memberService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(memberService) // DB에서 유저 찾는 서비스 연결
                .passwordEncoder(passwordEncoder); // 비밀번호 암호화 도구 연결

        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/join",
                                "/css/**", "/js/**", "/images/**",
                                "/mail/**", "/user/**",
                                "/policy", "/simulation", "/setup"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")   // 아이디 파라미터명
                        .passwordParameter("userPw")  // 비밀번호 파라미터명
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
            String email = authentication.getName();
            MemberDto member = memberMapper.selectMemberByEmail(email);

            if (member != null) {
                HttpSession session = request.getSession();
                session.setAttribute("loginMember", member);
            }
            response.sendRedirect("/policy");
        };
    }
}