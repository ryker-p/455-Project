package com.emr.dto;

import java.time.Instant;

public record AccessLogResponse(
    Long id,
    Long actorUserId,
    String actorEmail,
    String action,
    String resourceType,
    String resourceId,
    String ipAddress,
    Instant createdAt
) {}

