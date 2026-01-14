package com.simpol.polisight.service;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.mapper.MemberMapper;
import jakarta.servlet.http.HttpSession; // (스프링 부트 3.0 이상) 2.x버전이면 javax.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 소셜 로그인 API에서 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // ★★★ [여기서 토큰을 꺼냅니다!] ★★★
        String accessToken = userRequest.getAccessToken().getTokenValue();
        System.out.println("★ 소셜 Access Token 확보: " + accessToken);

        // ★★★ [세션에 토큰 저장하기] ★★★
        // 서비스단에서 세션을 잡기 위해 RequestContextHolder를 사용합니다.
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession();

            // 세션에 'socialAccessToken'이라는 이름으로 저장!
            // -> 나중에 탈퇴 컨트롤러에서 꺼내 쓸 수 있습니다.
            session.setAttribute("socialAccessToken", accessToken);
        } catch (Exception e) {
            System.out.println("세션 저장 중 오류 발생 (무시 가능): " + e.getMessage());
        }

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = "";
        String name = "";

        // 2. 소셜 별 데이터 추출
        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");

            if (email == null) email = String.valueOf(attributes.get("id")) + "@kakao.com";
            if (name == null) name = "카카오회원";
        }

        System.out.println("[" + registrationId + "] 로그인 성공 -> 이메일: " + email);

        // 3. DB 조회 및 가입
        MemberDto member = memberMapper.selectMemberByEmail(email);

        if (member == null) {
            member = new MemberDto();
            member.setEmail(email);
            member.setMemberName(name);
            member.setPasswordHash(UUID.randomUUID().toString());
            member.setProvider(registrationId);

            try {
                memberMapper.insertMember(member);
                System.out.println("★ 신규 회원가입 완료: " + email);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("★ 기존 회원 로그인: " + email);
        }

        // [중요] 세션에 로그인 회원 정보(loginMember)도 같이 넣어줘야 컨트롤러들이 작동합니다.
        // (만약 별도의 SuccessHandler가 없다면 여기서 넣어주는 게 안전합니다)
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession();
            session.setAttribute("loginMember", member); // loginMember 세션 저장
        } catch (Exception e) {}

        return new CustomOAuth2User(member, attributes);
    }

    // [내부 클래스]
    public static class CustomOAuth2User implements OAuth2User {
        private final MemberDto member;
        private final Map<String, Object> attributes;

        public CustomOAuth2User(MemberDto member, Map<String, Object> attributes) {
            this.member = member;
            this.attributes = attributes;
        }

        @Override
        public Map<String, Object> getAttributes() { return attributes; }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public String getName() { return member.getEmail(); }

        public MemberDto getMember() { return member; }
    }
}