package com.mindary.identity.controllers;

import com.mindary.identity.dto.request.VerifyTokenRequest;
import com.mindary.identity.dto.response.AuthResponse;
import com.mindary.identity.dto.request.LoginRequest;
import com.mindary.identity.dto.request.SignUpRequest;
import com.mindary.identity.dto.response.VerifyTokenResponse;
import com.mindary.identity.security.SystemUserDetails;
import com.mindary.identity.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Tag(name = "User Identity APIs", description = "These APIs are used for user authentication and authorization")
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "User Login", description = "Authenticate user and generate access and refresh tokens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login request: {}", loginRequest.getEmail());
        UserDetails userDetails = authenticationService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        UUID userId = extractUserId(userDetails);

        String accessToken = authenticationService.generateAccessToken(userDetails);
        String refreshToken = authenticationService.generateRefreshToken(userDetails);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .build();

        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "User Registration", description = "Register a new user and generate access and refresh tokens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful registration", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @PostMapping(path = "/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest signUpRequest) {
        UserDetails userDetails = authenticationService.registerUser(
                signUpRequest.getUsername(),
                signUpRequest.getPassword(),
                signUpRequest.getEmail(),
                signUpRequest.getRole()
        );

        UUID userId = extractUserId(userDetails);

        String accessToken = authenticationService.generateAccessToken(userDetails);
        String refreshToken = authenticationService.generateRefreshToken(userDetails);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userId)
                .build();

        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Verify Access Token", description = "Verify the validity of an access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VerifyTokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @PostMapping(path = "/verify-token")
    public ResponseEntity<VerifyTokenResponse> verifyToken(@RequestBody VerifyTokenRequest verifyTokenRequest) {
        log.info("Verify token request: {}", verifyTokenRequest.getToken());
        VerifyTokenResponse verifyTokenResponse = authenticationService.verifyAccessToken(verifyTokenRequest.getToken());
        if (verifyTokenResponse.isValid()) {
            return ResponseEntity.ok(verifyTokenResponse);
        }
        return new ResponseEntity<>(
                verifyTokenResponse,
                HttpStatus.UNAUTHORIZED
        );
    }

    @Operation(summary = "Refresh Token", description = "Generate new access and refresh tokens using a refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid refresh token", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(schema = @Schema())})
    })
    @PostMapping(path = "/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String accessToken = authenticationService.generateAccessToken(userDetails);
        String refreshToken = authenticationService.generateRefreshToken(userDetails);
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    private UUID extractUserId(UserDetails userDetails) {
        return ((SystemUserDetails) userDetails).getId();
    }
}
