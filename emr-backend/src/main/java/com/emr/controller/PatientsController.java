package com.emr.controller;

import com.emr.dto.PatientProfileResponse;
import com.emr.dto.PatientProfileUpdateRequest;
import com.emr.dto.PatientSearchResponse;
import com.emr.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientsController {
  private final PatientService patientService;

  public PatientsController(PatientService patientService) {
    this.patientService = patientService;
  }

  @GetMapping("/my-profile")
  @PreAuthorize("hasRole('PATIENT')")
  public PatientProfileResponse myProfile() {
    return patientService.getMyProfile();
  }

  @PutMapping("/my-profile")
  @PreAuthorize("hasRole('PATIENT')")
  public PatientProfileResponse updateMyProfile(@Valid @RequestBody PatientProfileUpdateRequest request) {
    return patientService.updateMyProfile(request);
  }

  @GetMapping("/{patientId}")
  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','ADMIN')")
  public PatientProfileResponse get(@PathVariable Long patientId) {
    return patientService.getById(patientId);
  }

  @GetMapping("/search")
  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','ADMIN')")
  public List<PatientSearchResponse> search(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) LocalDate dob,
      @RequestParam(required = false) Long patientId
  ) {
    return patientService.search(q, dob, patientId);
  }
}
