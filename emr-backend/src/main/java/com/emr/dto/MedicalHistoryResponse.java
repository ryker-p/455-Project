package com.emr.dto;

import java.time.Instant;

public record MedicalHistoryResponse(
    Long id,
    Long patientId,
    Long doctorId,
    String conditionName,
    String notes,
    Instant recordedAt
) {}

