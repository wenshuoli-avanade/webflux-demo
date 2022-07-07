package com.avanade.wsl.webfluxdemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * congifuration class, config upstream service url and inject WebClient to the container
 */
@Configuration
public class WebClientConfig {

    /**
     * Register the WebClient in the container for the product service to call the upStream service
     *
     * @param productBaseUrl   upStream service url, config in the application.yml
     * @param webClientBuilder injected by SpringBoot
     * @return WebClient
     */
    @Bean
    public WebClient productWebClient(
            @Value("${product_base_url}") String productBaseUrl,
            WebClient.Builder webClientBuilder) {

        return webClientBuilder
                .baseUrl(productBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
