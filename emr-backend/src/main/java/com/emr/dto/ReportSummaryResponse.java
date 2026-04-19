package com.emr.dto;

public record ReportSummaryResponse(
    long users,
    long patients,
    long doctors,
    long appointments,
    long prescriptions,
    long openBills
) {}

