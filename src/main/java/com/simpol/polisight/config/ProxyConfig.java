package com.simpol.polisight.config;

import org.apache.catalina.valves.RemoteIpValve;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addContextValves(createRemoteIpValve());
    }

    private RemoteIpValve createRemoteIpValve() {
        RemoteIpValve remoteIpValve = new RemoteIpValve();

        // 1. 로드밸런서가 보내는 헤더 이름 지정 (GCP 표준)
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProtocolHeader("x-forwarded-proto");
        remoteIpValve.setProtocolHeaderHttpsValue("https");

        // 2. 가장 중요: 모든 내부 프록시 IP를 신뢰하도록 강제 설정 (Regex)
        // 설정 파일에서 안 먹히던 게 여기서 하면 무조건 먹힙니다.
        remoteIpValve.setInternalProxies(".*");

        return remoteIpValve;
    }
}
