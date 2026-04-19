package com.emr.dto;

public record AuthResponse(
    String token,
    MeResponse me
) {}

