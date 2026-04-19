package com.emr.dto;

public record DoctorAppointmentReportRow(
    Long doctorId,
    String doctorName,
    long total,
    long scheduled,
    long confirmed,
    long completed,
    long cancelled
) {}

