package com.emr.dto;

public record TwoFactorSetupResponse(
    Long userId,
    boolean enabled,
    String secret,
    String otpAuthUri
) {}

