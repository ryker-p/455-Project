package com.emr.controller;

import com.emr.dto.MedicalHistoryCreateRequest;
import com.emr.dto.MedicalHistoryResponse;
import com.emr.service.MedicalHistoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-history")
public class MedicalHistoryController {
  private final MedicalHistoryService medicalHistoryService;

  public MedicalHistoryController(MedicalHistoryService medicalHistoryService) {
    this.medicalHistoryService = medicalHistoryService;
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('PATIENT')")
  public List<MedicalHistoryResponse> myHistory() {
    return medicalHistoryService.listMyHistory();
  }

  @GetMapping("/patient/{patientId}")
  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','ADMIN','LABTECH')")
  public List<MedicalHistoryResponse> forPatient(@PathVariable Long patientId) {
    return medicalHistoryService.listForPatient(patientId);
  }

  @PostMapping("/patient/{patientId}")
  @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
  public MedicalHistoryResponse add(@PathVariable Long patientId, @Valid @RequestBody MedicalHistoryCreateRequest request) {
    return medicalHistoryService.add(patientId, request);
  }
}
