package com.emr.dto;

import java.time.LocalDate;

public record PatientProfileResponse(
    Long patientId,
    Long userId,
    String username,
    String email,
    String firstName,
    String lastName,
    String maskedSsn,
    LocalDate dateOfBirth,
    String sex,
    String phone,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String zip,
    Long insuranceId,
    String emergencyContactName,
    String emergencyContactPhone
) {}
