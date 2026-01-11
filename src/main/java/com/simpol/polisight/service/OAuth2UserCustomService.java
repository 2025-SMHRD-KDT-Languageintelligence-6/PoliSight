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
        // 1. 구글 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // 2. DB 조회
        MemberDto member = memberMapper.selectMemberByEmail(email);

        // 3. 없으면 자동 가입
        if (member == null) {
            MemberDto newMember = new MemberDto();
            newMember.setEmail(email);

            // ★ 수정됨: 구글 이름을 memberName에 넣음
            newMember.setMemberName(name);

            // ★ 수정됨: 랜덤 비밀번호를 passwordHash에 넣음
            newMember.setPasswordHash(UUID.randomUUID().toString());

            // birthDate는 DTO에서 비워두면 자동으로 null로 들어감

            memberMapper.insertMember(newMember);
            System.out.println("구글 신규 회원가입 완료: " + email);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email");
    }
}