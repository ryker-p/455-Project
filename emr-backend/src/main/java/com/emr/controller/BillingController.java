package com.emr.controller;

import com.emr.dto.BillingCreateRequest;
import com.emr.dto.BillingResponse;
import com.emr.dto.BillingStatusUpdateRequest;
import com.emr.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillingController {
  private final BillingService billingService;

  public BillingController(BillingService billingService) {
    this.billingService = billingService;
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('PATIENT')")
  public List<BillingResponse> myBilling() {
    return billingService.listMyBilling();
  }

  @GetMapping("/patient/{patientId}")
  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','ADMIN')")
  public List<BillingResponse> forPatient(@PathVariable Long patientId) {
    return billingService.listForPatient(patientId);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
  public BillingResponse create(@Valid @RequestBody BillingCreateRequest request) {
    return billingService.create(request);
  }

  @PutMapping("/{billingId}/status")
  @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
  public BillingResponse updateStatus(@PathVariable Long billingId, @Valid @RequestBody BillingStatusUpdateRequest request) {
    return billingService.updateStatus(billingId, request);
  }
}

