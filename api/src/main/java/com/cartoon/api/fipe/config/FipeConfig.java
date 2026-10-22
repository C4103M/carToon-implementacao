package com.cartoon.api.fipe.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableCaching
@EnableConfigurationProperties(FipeProperties.class)
public class FipeConfig {

    @Bean
    public RestClient fipeRestClient(FipeProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        String baseUrl = (properties.baseUrl() != null && !properties.baseUrl().isBlank())
                ? properties.baseUrl().trim()
                : "https://fipe.parallelum.com.br/api/v2";

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory);

        if (properties.token() != null && !properties.token().isBlank()) {
            builder.defaultHeader("X-Subscription-Token", properties.token().trim());
        }

        return builder.build();
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "fipe-marcas", "fipe-modelos", "fipe-anos", "fipe-valor"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofHours(24)));
        return cacheManager;
    }
}
