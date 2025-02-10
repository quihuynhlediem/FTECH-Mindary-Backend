package com.mindary.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        http
                .cors((cors) -> cors
                        .configurationSource(corsConfigurationSource())
                )
                .csrf(csrf -> {
//                    csrf.csrfTokenRepository(new CookieServerCsrfTokenRepository());
//                    log.info("CSRF protection configured");
                    csrf.disable();
                })
                .authorizeExchange(exchange -> {
                    exchange
                            // Allow OPTIONS requests for preflight (important for CORS)
//                            .pathMatchers(HttpMethod.POST, "/**").permitAll()
//                            .pathMatchers("/api/v1/auth/login", "/api/v1/auth/signup").permitAll()
                            .anyExchange().permitAll();
                })
        ;
        return http.build();
    }

    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CorsConfigurationSource");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

//@EnableWebSecurity
//public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain springSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> {
//                    csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
//                })
//                .authorizeHttpRequests(
//                        auth -> auth.anyRequest().permitAll()
//                );
//        return http.build();
//    }
//}
