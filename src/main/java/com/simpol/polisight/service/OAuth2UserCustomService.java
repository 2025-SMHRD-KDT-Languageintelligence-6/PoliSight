package com.simpol.polisight.service;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    // 강제 형변환 경고 무시
    @SuppressWarnings("unchecked")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 소셜 로그인 API에서 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

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
            name = (String) kakaoAccount.get("name");

            // 데이터 없을 경우 방어 코드
            if (email == null) email = String.valueOf(attributes.get("id")) + "@kakao.com";
            if (name == null) name = "카카오회원";
        }

        System.out.println("[" + registrationId + "] 로그인 성공 -> 이메일: " + email);

        // 3. DB 조회 및 가입
        MemberDto member = memberMapper.selectMemberByEmail(email);

        if (member == null) {
            member = new MemberDto(); // member 변수 재사용
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

        // 4. [핵심] DefaultOAuth2User 대신 우리가 만든 'CustomOAuth2User'를 리턴
        // 이렇게 하면 스프링이 "숫자 ID"가 아니라 우리가 정한 "이메일"을 진짜 ID로 인식합니다.
        return new CustomOAuth2User(member, attributes);
    }

    // [내부 클래스] 무적의 로그인 객체
    // 이 클래스가 "제 이름은 이메일입니다"라고 강력하게 주장합니다.
    public static class CustomOAuth2User implements OAuth2User {
        private final MemberDto member;
        private final Map<String, Object> attributes;

        public CustomOAuth2User(MemberDto member, Map<String, Object> attributes) {
            this.member = member;
            this.attributes = attributes;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public String getName() {
            // ★ 여기가 핵심입니다!
            // 구글/카카오 ID가 아니라, 우리 DB의 '이메일'을 이름으로 리턴합니다.
            return member.getEmail();
        }

        // 필요시 회원 정보를 바로 꺼낼 수 있는 보너스 메소드
        public MemberDto getMember() {
            return member;
        }
    }
}