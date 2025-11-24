package com.asre.asre.domain.auth;

import com.asre.asre.domain.user.User;
import lombok.Value;

@Value
public class AuthResult {
    String accessToken;
    String refreshToken;
    User user;
}
