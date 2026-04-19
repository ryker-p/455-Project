package com.emr.dto;

import jakarta.validation.constraints.NotBlank;

public record PrescriptionStatusUpdateRequest(
    @NotBlank String status
) {}

