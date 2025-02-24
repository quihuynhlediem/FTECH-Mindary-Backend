package com.mindary.api_gateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindary.api_gateway.dto.VerifyTokenResponse;
import com.mindary.api_gateway.services.IdentityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final IdentityService identityService;
    private final ObjectMapper objectMapper;

    @NonFinal
    private final String[] publicEndpoints = {
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/api/v1/auth/verify-token",
            "/identity-service/v3/api-docs",
            "/diary-entry-service/v3/api-docs",
            "/meditation-recommendation-service",
            "/ai-chat-service/v3/api-docs",
            "/api/v1/customers/forgot-password",
            "/api/v1/customers/validate-otp",
            "/api/v1/customers/new-password",
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // If this is public api -> continue to next filter
        if (isPublicEndpoint(exchange.getRequest())) {
            log.info(exchange.getRequest().getURI().toString());
            return chain.filter(exchange);
        }

        // Get token from authorization header
        List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeaders)) {
            return unauthenticated(exchange.getResponse());
        }

        // Verify token + Delegate service
        String token = authHeaders.getFirst().replace("Bearer ", "");
        log.info("token: {}", token);

        return identityService.verifyToken(token).flatMap(verifyTokenResponseResponseEntity -> {
            VerifyTokenResponse verifyTokenResponse = verifyTokenResponseResponseEntity.getBody();

            assert verifyTokenResponse != null;
            if (verifyTokenResponse.isValid()) {
                return chain.filter(exchange);
            } else {
                return unauthenticated(exchange.getResponse());
            }
        }).onErrorResume(throwable -> unauthenticated(exchange.getResponse()));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return Arrays.stream(publicEndpoints).anyMatch(
                endpoint -> request.getURI().getPath().matches(endpoint)
        );
    }

    Mono<Void> unauthenticated(ServerHttpResponse response) {
        String body = null;
        try {
            VerifyTokenResponse verifyTokenResponse = VerifyTokenResponse.builder()
                    .message("Unauthenticated").build();
            body = objectMapper.writeValueAsString(verifyTokenResponse);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        assert body != null;
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }
}
