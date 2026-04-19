package com.emr.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
    @NotBlank String identifier,
    @NotBlank String password,
    String twoFactorCode
) {}
