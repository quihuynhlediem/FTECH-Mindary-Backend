package com.mindary.api_gateway.repositories;


import com.mindary.api_gateway.dto.VerifyTokenRequest;
import com.mindary.api_gateway.dto.VerifyTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@Component
public interface IdentityClient {
    @PostExchange(url = "/api/v1/auth/verify-token", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<VerifyTokenResponse>> verifyToken(@RequestBody VerifyTokenRequest verifyTokenRequest);
}