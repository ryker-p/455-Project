package com.emr.dto;

import jakarta.validation.constraints.Future;

import java.time.Instant;

public record AppointmentUpdateRequest(
    @Future Instant scheduledAt,
    String status,
    String notes
) {}

