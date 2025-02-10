package com.mindary.diary.services;

import io.jsonwebtoken.Claims;

public interface AuthenticationService {
    Claims extractTokenInfo(String token);
}
