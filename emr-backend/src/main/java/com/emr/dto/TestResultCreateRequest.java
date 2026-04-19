package com.emr.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TestResultCreateRequest(
    @NotBlank String testName,
    @NotBlank String resultValue,
    String units,
    String normalRange,
    LocalDate resultDate,
    String notes
) {}

