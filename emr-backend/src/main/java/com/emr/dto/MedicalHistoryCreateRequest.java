package com.emr.dto;

import jakarta.validation.constraints.NotBlank;

public record MedicalHistoryCreateRequest(
    @NotBlank String conditionName,
    String notes
) {}

