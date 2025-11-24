package com.asre.asre.domain.auth;

import lombok.Value;

@Value
public class RefreshCommand {
    String refreshToken;
}
