package com.asre.asre.domain.auth;

import lombok.Value;

@Value
public class LogoutCommand {
    String refreshToken;
}
