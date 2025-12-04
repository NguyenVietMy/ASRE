package com.asre.asre.application.auth;

import lombok.Value;

@Value
public class LogoutCommand {
    String refreshToken;
}

