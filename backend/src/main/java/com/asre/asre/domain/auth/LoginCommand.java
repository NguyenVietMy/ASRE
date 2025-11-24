package com.asre.asre.domain.auth;

import lombok.Value;

@Value
public class LoginCommand {
    String email;
    String password;
}

