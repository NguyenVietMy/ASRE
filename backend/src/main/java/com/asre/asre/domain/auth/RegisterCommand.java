package com.asre.asre.domain.auth;

import lombok.Value;

@Value
public class RegisterCommand {
    String email;
    String password;
}
