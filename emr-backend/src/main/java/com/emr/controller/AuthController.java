package com.emr.controller;

import com.emr.dto.AuthLoginRequest;
import com.emr.dto.AuthResponse;
import com.emr.dto.AuthSignupRequest;
import com.emr.dto.ResetPasswordRequest;
import com.emr.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/register")
  public AuthResponse register(@Valid @RequestBody AuthSignupRequest request) {
    return authService.registerPatient(request);
  }

  @PostMapping("/reset-password")
  public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
  }
}

