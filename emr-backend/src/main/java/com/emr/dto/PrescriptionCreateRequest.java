package com.emr.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record PrescriptionCreateRequest(
    @NotBlank String medicationName,
    @NotBlank String dosage,
    @NotBlank String instructions,
    LocalDate startDate,
    LocalDate endDate
) {}

