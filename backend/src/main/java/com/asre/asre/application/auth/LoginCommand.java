package com.asre.asre.application.auth;

import lombok.Value;

@Value
public class LoginCommand {
    String email;
    String password;
}

