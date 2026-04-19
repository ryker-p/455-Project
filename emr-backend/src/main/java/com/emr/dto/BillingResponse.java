package com.emr.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BillingResponse(
    Long id,
    Long patientId,
    Long appointmentId,
    BigDecimal amount,
    String status,
    LocalDate dueDate,
    String description,
    Instant createdAt
) {}

