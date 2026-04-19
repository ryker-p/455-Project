package com.emr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthSignupRequest(
    @NotBlank @Size(min = 3, max = 60) String username,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 72) String password,
    @NotBlank String firstName,
    @NotBlank String lastName
) {}
