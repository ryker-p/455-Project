package com.emr.dto;

import java.time.Instant;

public record AppointmentResponse(
    Long id,
    Long patientId,
    String patientName,
    Long doctorId,
    String doctorName,
    Instant scheduledAt,
    String status,
    String reason,
    String notes
) {}

