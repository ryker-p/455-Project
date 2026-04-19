package com.emr.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BillingCreateRequest(
    @NotNull Long patientId,
    Long appointmentId,
    @NotNull @DecimalMin("0.00") BigDecimal amount,
    @NotNull LocalDate dueDate,
    String description
) {}

