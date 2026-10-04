package com.cartoon.api.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
                    .requestMatchers(HttpMethod.GET, "/pecas/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                    .requestMatchers(HttpMethod.POST, "/pecas/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                    .requestMatchers(HttpMethod.PUT, "/pecas/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/pecas/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                    .requestMatchers(HttpMethod.GET, "/sericos/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                    .requestMatchers(HttpMethod.POST, "/sericos/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                    .requestMatchers(HttpMethod.PUT, "/sericos/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/sericos/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")

            );
        return http.build();
    }
}
