package com.mindary.identity.controllers;

import com.mindary.identity.dto.request.VerifyTokenRequest;
import com.mindary.identity.dto.response.AuthResponse;
import com.mindary.identity.dto.request.LoginRequest;
import com.mindary.identity.dto.request.SignUpRequest;
import com.mindary.identity.dto.response.VerifyTokenResponse;
import com.mindary.identity.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        UserDetails userDetails = authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
        String token = authenticationService.generateToken(userDetails);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .expiresIn(86400)
                .build();

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping(path = "/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
        UserDetails userDetails = authenticationService.registerUser(
                signUpRequest.getUsername(),
                signUpRequest.getPassword(),
                signUpRequest.getEmail(),
                signUpRequest.getRole()
        );

        String token = authenticationService.generateToken(userDetails);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .expiresIn(86400)
                .build();

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping(path = "/verifyToken")
    public ResponseEntity<VerifyTokenResponse> verifyToken(@RequestBody VerifyTokenRequest verifyTokenRequest) {
        VerifyTokenResponse verifyTokenResponse = authenticationService.verifyToken(verifyTokenRequest.getToken());
        if (verifyTokenResponse.isValid()) {
            return ResponseEntity.ok(verifyTokenResponse);
        }
        return new ResponseEntity<>(
                verifyTokenResponse,
                HttpStatus.UNAUTHORIZED
        );
    }
}
