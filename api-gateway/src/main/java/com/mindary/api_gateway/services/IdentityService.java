package com.mindary.api_gateway.services;

import com.mindary.api_gateway.dto.VerifyTokenRequest;
import com.mindary.api_gateway.dto.VerifyTokenResponse;
import com.mindary.api_gateway.repositories.IdentityClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityService {

    IdentityClient identityClient;

    public Mono<ResponseEntity<VerifyTokenResponse>> verifyToken(String token) {
        return identityClient.verifyToken(
                VerifyTokenRequest.builder()
                .token(token)
                .build()
        );
    }
}
