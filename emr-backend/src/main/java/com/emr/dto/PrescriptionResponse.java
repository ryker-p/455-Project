package com.emr.dto;

import java.time.Instant;
import java.time.LocalDate;

public record PrescriptionResponse(
    Long id,
    Long patientId,
    Long doctorId,
    String medicationName,
    String dosage,
    String instructions,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    Instant createdAt
) {}

