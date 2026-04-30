package com.emr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AuthSignupRequest(
    @NotBlank @Size(min = 3, max = 60) String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 72) String password,
    @NotBlank String firstName,
    @NotBlank String lastName,
    LocalDate dateOfBirth,
    String ssn,
    String phone,
    String address,
    String sex,
    // Insurance fields (all optional)
    String insuranceProvider,
    String policyNumber,
    String groupNumber,
    LocalDate effectiveDate
) {}
