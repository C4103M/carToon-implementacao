package com.cartoon.api.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth

                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/ordem-servico/**").hasAnyRole("ADMIN", "MECANICO")
                    .requestMatchers(HttpMethod.PUT, "/ordem-servico/**").hasAnyRole("ADMIN", "MECANICO")
                    .requestMatchers(HttpMethod.DELETE, "/ordem-servico/**").hasAnyRole("ADMIN", "MECANICO")
                    .requestMatchers(HttpMethod.GET, "/ordem-servico/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
            );
        return http.build();
    }
}
