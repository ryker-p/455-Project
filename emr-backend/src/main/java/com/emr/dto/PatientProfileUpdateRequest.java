package com.emr.dto;

public record PatientProfileUpdateRequest(
    String phone,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String zip,
    String emergencyContactName,
    String emergencyContactPhone
) {}
