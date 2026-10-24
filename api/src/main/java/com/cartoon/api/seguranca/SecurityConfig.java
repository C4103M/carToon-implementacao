package com.cartoon.api.seguranca;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:[*]",
                "http://127.0.0.1:[*]",
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   @Autowired(required = false) JwtCookieFilter jwtCookieFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers("/fipe/**").authenticated()

                        .requestMatchers("/clientes/**").hasAnyRole("MECANICO", "ADMIN")
                        .requestMatchers("/veiculos/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")

                        .requestMatchers(HttpMethod.POST, "/ordem-servico/**").hasAnyRole("ADMIN", "MECANICO")
                        .requestMatchers(HttpMethod.PUT, "/ordem-servico/**").hasAnyRole("ADMIN", "MECANICO")
                        .requestMatchers(HttpMethod.DELETE, "/ordem-servico/**").hasAnyRole("ADMIN", "MECANICO")
                        .requestMatchers(HttpMethod.GET, "/ordem-servico/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")

                        .requestMatchers(HttpMethod.GET, "/pecas/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                        .requestMatchers(HttpMethod.POST, "/pecas/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/pecas/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/pecas/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")

                        .requestMatchers(HttpMethod.GET, "/servicos/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                        .requestMatchers(HttpMethod.POST, "/servicos/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/servicos/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/servicos/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")

                        .requestMatchers(HttpMethod.GET, "/oficina/**").hasAnyRole("ADMIN", "MECANICO", "SUPERADMIN")
                        .requestMatchers(HttpMethod.POST, "/oficina").hasRole("SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/oficina/*").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/oficina/*/ativar", "/oficina/*/desativar").hasRole("SUPERADMIN")

                        .requestMatchers(HttpMethod.GET, "/usuario/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/usuario/me").authenticated()

                        .requestMatchers(HttpMethod.POST, "/usuario/mecanico").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/usuario/admin").hasRole("SUPERADMIN")

                        .requestMatchers(HttpMethod.GET, "/usuario", "/usuario/*").hasAnyRole("ADMIN", "SUPERADMIN", "MECANICO")
                        .requestMatchers(HttpMethod.PUT, "/usuario/*").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/usuario/*").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/usuario/*/ativar").hasAnyRole("ADMIN", "SUPERADMIN")

                        .anyRequest().authenticated()
                );
        if (jwtCookieFilter != null) {
            http.addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }
}
