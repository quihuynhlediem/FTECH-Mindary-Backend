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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    private final Long jwtExpiryMs = 86400000L;

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
    public VerifyTokenResponse verifyToken(String token) {
        try {

            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            Claims body = claimsJws.getBody();
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
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
        claims.put("role", role);

        UUID userId = ((SystemUserDetails) userDetails).getId();
        claims.put("userId", userId.toString());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public UserDetails validateToken(String token) {
        String email = extractUsername(token);
        return userDetailsService.loadUserByUsername(email);
    }

    private String extractUsername(String token) {
        Claims claim = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claim.getSubject();
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
