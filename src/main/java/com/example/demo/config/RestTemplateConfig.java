package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate smsRestTemplate(RestTemplateBuilder builder,
                                        @Value("${app.http.connect-timeout:2000}") long connectTimeoutMs,
                                        @Value("${app.http.read-timeout:3000}") long readTimeoutMs) {
        return builder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                    requestFactory.setConnectTimeout(Math.toIntExact(connectTimeoutMs));
                    requestFactory.setReadTimeout(Math.toIntExact(readTimeoutMs));
                    return requestFactory;
                })
                .build();
    }
}
