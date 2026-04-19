package com.emr.dto;

public record MeResponse(
    Long userId,
    String username,
    String email,
    String role,
    Long patientId,
    Long doctorId,
    Long nurseId,
    Long labTechId,
    Long adminId,
    String displayName,
    boolean twoFactorEnabled
) {}
