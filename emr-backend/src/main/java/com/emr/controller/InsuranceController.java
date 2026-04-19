package com.emr.controller;

import com.emr.dto.InsuranceRequest;
import com.emr.dto.InsuranceResponse;
import com.emr.service.InsuranceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insurance")
public class InsuranceController {
  private final InsuranceService insuranceService;

  public InsuranceController(InsuranceService insuranceService) {
    this.insuranceService = insuranceService;
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('PATIENT')")
  public List<InsuranceResponse> myInsurance() {
    return insuranceService.listMyInsurance();
  }

  @GetMapping("/patient/{patientId}")
  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','ADMIN')")
  public List<InsuranceResponse> forPatient(@PathVariable Long patientId) {
    return insuranceService.listForPatient(patientId);
  }

  @PostMapping("/patient/{patientId}")
  @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
  public InsuranceResponse upsert(@PathVariable Long patientId, @Valid @RequestBody InsuranceRequest request) {
    return insuranceService.upsert(patientId, request);
  }
}

