package com.mindary.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("identity-service", r -> r
                        .path("/api/v1/auth/**")
                        .uri("http://localhost:8081"))
                .route("customer-route", r -> r
                        .path("/api/v1/customers/**")
                        .uri("http://localhost:8081"))
                .route("diary-entry-route", r -> r
                        .path("/api/v1/diaries/**")
                        .uri("http://localhost:8082"))
                .route("ai-chat-service-route", r -> r
                        .path("/api/v1/chat/**")
                        .uri("http://localhost:8083")
                )
                .route("meditation-recommendation-service-route", r -> r
                        .path("api/v1/meditations")
                        .uri("http://localhost:8084")
                )
                .build();
    }
}