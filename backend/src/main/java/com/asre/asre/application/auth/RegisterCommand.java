package com.asre.asre.application.auth;

import lombok.Value;

@Value
public class RegisterCommand {
    String email;
    String password;
}

