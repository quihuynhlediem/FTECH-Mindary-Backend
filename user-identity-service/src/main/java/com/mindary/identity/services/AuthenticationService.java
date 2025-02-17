package com.mindary.identity.services;

import com.mindary.identity.dto.response.VerifyTokenResponse;
import com.mindary.identity.models.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
    UserDetails authenticate(String username, String password);
    String generateAccessToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    UserDetails validateToken(String token);
    UserDetails registerUser(String userName, String password, String email, User.UserRole userRole);
    VerifyTokenResponse verifyAccessToken(String token);
}
