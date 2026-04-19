package com.emr.dto;

import java.time.Instant;

public record UserListResponse(
    Long userId,
    String username,
    String email,
    String role,
    boolean enabled,
    Instant createdAt,
    String displayName,
    boolean twoFactorEnabled
) {}
