package com.emr.dto;

import java.time.LocalDate;

public record InsuranceResponse(
    Long id,
    Long patientId,
    String providerName,
    String policyNumber,
    String groupNumber,
    LocalDate effectiveDate,
    LocalDate expirationDate
) {}

