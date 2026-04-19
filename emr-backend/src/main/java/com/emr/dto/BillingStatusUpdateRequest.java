package com.emr.dto;

import jakarta.validation.constraints.NotBlank;

public record BillingStatusUpdateRequest(
    @NotBlank String status
) {}

