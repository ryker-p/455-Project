package com.emr.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AppointmentCreateRequest(
    @NotNull Long doctorId,
    @NotNull @Future Instant scheduledAt,
    @NotBlank String reason
) {}

