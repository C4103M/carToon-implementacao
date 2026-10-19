package com.cartoon.api.fipe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fipe")
public record FipeProperties(
        String baseUrl,
        String token
) {}
