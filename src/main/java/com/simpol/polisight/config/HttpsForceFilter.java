package com.simpol.polisight.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 모든 필터 중 가장 먼저 실행
public class HttpsForceFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String domain = req.getServerName();

        // 로컬 개발 환경(localhost)이 아닐 때만 강제로 HTTPS로 변환
        if (!domain.equals("localhost") && !domain.equals("127.0.0.1")) {
            chain.doFilter(new HttpsRequestWrapper(req), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    // 요청 정보를 조작하는 래퍼 클래스
    private static class HttpsRequestWrapper extends HttpServletRequestWrapper {
        public HttpsRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getScheme() {
            return "https"; // 무조건 https라고 거짓말 함
        }

        @Override
        public boolean isSecure() {
            return true; // 무조건 보안 연결이라고 거짓말 함
        }

        @Override
        public int getServerPort() {
            return 443; // 무조건 443 포트라고 거짓말 함
        }

        @Override
        public StringBuffer getRequestURL() {
            // 원본 URL에서 http:// -> https:// 로 바꿔치기
            String originalUrl = super.getRequestURL().toString();
            if (originalUrl.startsWith("http://")) {
                return new StringBuffer(originalUrl.replaceFirst("http://", "https://"));
            }
            return new StringBuffer(originalUrl);
        }
    }
}