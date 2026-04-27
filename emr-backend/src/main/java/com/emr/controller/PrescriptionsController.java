package com.emr.controller;

import com.emr.dto.PrescriptionCreateRequest;
import com.emr.dto.PrescriptionResponse;
import com.emr.dto.PrescriptionStatusUpdateRequest;
import com.emr.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionsController {
  private final PrescriptionService prescriptionService;

  public PrescriptionsController(PrescriptionService prescriptionService) {
    this.prescriptionService = prescriptionService;
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('PATIENT')")
  public List<PrescriptionResponse> myPrescriptions() {
    return prescriptionService.listMyPrescriptions();
  }

  @GetMapping("/patient/{patientId}")
  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','ADMIN')")
  public List<PrescriptionResponse> forPatient(@PathVariable Long patientId) {
    return prescriptionService.listForPatient(patientId);
  }

  @PostMapping("/patient/{patientId}")
  @PreAuthorize("hasRole('DOCTOR')")
  public PrescriptionResponse create(@PathVariable Long patientId, @Valid @RequestBody PrescriptionCreateRequest request) {
    return prescriptionService.create(patientId, request);
  }

  @PutMapping("/{prescriptionId}/status")
  @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
  public PrescriptionResponse updateStatus(@PathVariable Long prescriptionId, @Valid @RequestBody PrescriptionStatusUpdateRequest request) {
    return prescriptionService.updateStatus(prescriptionId, request);
  }
}
