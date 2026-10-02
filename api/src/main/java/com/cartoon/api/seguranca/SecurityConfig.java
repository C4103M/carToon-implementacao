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
                    .requestMatchers(HttpMethod.POST, "/orgem-servico/**").hasAnyRole("ADMIN", "MECANICO")
                    .requestMatchers(HttpMethod.PUT, "/orgem-servico/**").hasAnyRole("ADMIN", "MECANICO")
                    .requestMatchers(HttpMethod.DELETE, "/orgem-servico/**").hasAnyRole("ADMIN", "MECANICO")
                    .requestMatchers(HttpMethod.GET, "/orgem-servico/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
            );
        return http.build();
    }
}
