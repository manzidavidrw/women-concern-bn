package com.womenconcern.api.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient brevoWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .build();
    }
}
