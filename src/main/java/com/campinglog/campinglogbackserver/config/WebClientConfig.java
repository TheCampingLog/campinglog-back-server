package com.campinglog.campinglogbackserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${external.camp.base-url}")
    private String campBaseUrl;

    @Bean("campWebClient")
    public WebClient campWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(campBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
