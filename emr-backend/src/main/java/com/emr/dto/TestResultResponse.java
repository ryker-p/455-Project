package com.emr.dto;

import java.time.Instant;
import java.time.LocalDate;

public record TestResultResponse(
    Long id,
    Long patientId,
    Long doctorId,
    String testName,
    String resultValue,
    String units,
    String normalRange,
    LocalDate resultDate,
    String notes,
    Instant createdAt
) {}

