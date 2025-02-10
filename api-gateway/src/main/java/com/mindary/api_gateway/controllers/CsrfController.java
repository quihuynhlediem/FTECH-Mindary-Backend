package com.mindary.api_gateway.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/csrf")
@Slf4j
public class CsrfController {
    @GetMapping
    public Mono<CsrfToken> getCsrfToken(ServerWebExchange exchange) {
        // Force token generation by accessing it (if it doesn't exist, it's created):
        Mono<CsrfToken> csrfTokenMono = exchange.getFormData().flatMap(formData -> {
            // This line is the KEY: It forces CSRF token generation
            CsrfToken token = exchange.getAttribute(CsrfToken.class.getName());
            log.info("Csrf token: {}", token);
            return Mono.justOrEmpty(token);
        });

        return csrfTokenMono.switchIfEmpty(Mono.defer(() -> {
            CsrfToken token = exchange.getAttribute(CsrfToken.class.getName());
            return Mono.justOrEmpty(token);
        }));
    }
}
