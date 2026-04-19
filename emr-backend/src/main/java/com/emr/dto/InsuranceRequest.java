package com.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InsuranceRequest(
    @NotBlank String providerName,
    @NotBlank String policyNumber,
    String groupNumber,
    @NotNull LocalDate effectiveDate,
    LocalDate expirationDate
) {}

