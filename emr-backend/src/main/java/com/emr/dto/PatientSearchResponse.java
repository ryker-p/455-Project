package com.emr.dto;

import java.time.LocalDate;

public record PatientSearchResponse(
    Long patientId,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String email
) {}

