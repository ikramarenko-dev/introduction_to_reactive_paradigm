package com.gd.reactiveparadigm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient orderSearchWebClient(@Value("${services.order-search.url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient productInfoWebClient(@Value("${services.product-info.url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
