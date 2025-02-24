package com.mindary.aichat.services;

import io.jsonwebtoken.Claims;

public interface AuthenticationService {
    Claims extractTokenInfo(String token);
}
