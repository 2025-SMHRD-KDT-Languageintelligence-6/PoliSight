package com.simpol.polisight.service;

import com.simpol.polisight.dto.MemberDto;
import com.simpol.polisight.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 소셜 로그인 API에서 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 어떤 소셜 로그인인지 확인 (google vs kakao)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String email = "";
        String name = "";

        // 3. 소셜 별로 데이터 꺼내는 방법 분기 처리
        if ("google".equals(registrationId)) {
            // [구글]
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");

        } else if ("kakao".equals(registrationId)) {
            // [카카오] 변경됨: 닉네임(profile) 대신 실명(name) 가져오기
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

            // 1. 이메일
            email = (String) kakaoAccount.get("email");

            // 2. 이름 (name)
            name = (String) kakaoAccount.get("name");
        }

        System.out.println("[" + registrationId + "] 로그인 시도: " + email);

        // 4. DB 조회 (기존 로직과 동일)
        MemberDto member = memberMapper.selectMemberByEmail(email);

        // 5. 없으면 자동 가입
        if (member == null) {
            MemberDto newMember = new MemberDto();
            newMember.setEmail(email);
            newMember.setMemberName(name); // 이름 저장
            newMember.setPasswordHash(UUID.randomUUID().toString()); // 랜덤 비번
            // newMember.setBirthDate(null); // 생일은 비워둠

            memberMapper.insertMember(newMember);
            System.out.println("신규 소셜 회원 가입 완료 (" + registrationId + ")");
        }

        // 6. 리턴 (수정된 만능 코드)

        // 구글은 "sub"이나 "email", 카카오는 "id"...
        // 이걸 일일이 적지 말고, 설정 파일에서 알아서 가져오게 시킵니다.
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName);
    }
}