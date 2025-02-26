package com.mindary.identity.services.impl;

import com.mindary.identity.dto.response.VerifyTokenResponse;
import com.mindary.identity.models.CustomerEntity;
import com.mindary.identity.models.User;
import com.mindary.identity.security.SystemUserDetails;
import com.mindary.identity.services.AuthenticationService;
import com.mindary.identity.services.CustomerService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;

    @Value(value = "${jwt.secret:application.properties}")
    private String secretKey;

    @Value(value = "${otp.characters:application.properties}")
    private String otpCharacters;

    @Value(value = "${otp.length:application.properties}")
    private String otpLength;

    private final Long jwtExpiryMs = 86400000L;
    private final Long refreshTokenExpiryMs = 86400000L * 7;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public UserDetails authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        return userDetailsService.loadUserByUsername(email);
    }

    @Override
    public UserDetails registerUser(String userName, String password, String email, User.UserRole userRole) {
        if (userRole.equals(User.UserRole.CUSTOMER)) {
            CustomerEntity tenant = CustomerEntity.builder()
                    .username(userName)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(userRole)
                    .build();
            customerService.save(tenant);
        }
        return userDetailsService.loadUserByUsername(email);
    }

    @Override
    public VerifyTokenResponse verifyAccessToken(String token) {
        try {
            Claims body = extractAllClaims(token);
            Date expirationDate = body.getExpiration();

            if (expirationDate.before(new Date())) {
                return VerifyTokenResponse.builder()
                        .valid(false)
                        .message("Token has expired")
                        .build();
            } else {
                return VerifyTokenResponse.builder()
                        .valid(true)
                        .message("Token is valid")
                        .build();
            }
        } catch (JwtException e) {
            log.error("Invalid JWT Token: {}", e.getMessage());
            return VerifyTokenResponse.builder()
                    .valid(false)
                    .message("Invalid JWT Token")
                    .build();
        }
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
        claims.put("role", role);

        UUID userId = ((SystemUserDetails) userDetails).getId();
        claims.put("userId", userId.toString());

        return buildToken(claims, userDetails, jwtExpiryMs);
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, userDetails, refreshTokenExpiryMs);
    }

    @Override
    public UserDetails validateToken(String token) {
        if (isTokenExpired(token)) {
                log.info("Invalid JWT Token: {}", token);
                throw new JwtException("Invalid JWT Token");
        }
        String email = extractUsername(token);
        return userDetailsService.loadUserByUsername(email);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public String generateOTP() {
        log.info("Generating OTP");
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < Integer.parseInt(otpLength); i++) {
            otp.append(otpCharacters.charAt(secureRandom.nextInt(otpCharacters.length())));
        }
        return otp.toString();
    }

    @Override
    public LocalDateTime generateExpiryDateTime() {
        return LocalDateTime.now().plusMinutes(10);
    }

    private String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    private Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
