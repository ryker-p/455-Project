package com.emr.dto;

import jakarta.validation.constraints.NotBlank;

public record AppointmentStatusUpdateRequest(
    @NotBlank String status,
    String notes
) {}

