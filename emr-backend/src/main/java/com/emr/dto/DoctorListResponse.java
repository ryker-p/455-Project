package com.emr.dto;

public record DoctorListResponse(
    Long doctorId,
    String firstName,
    String lastName,
    String specialty
) {}

