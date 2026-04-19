package com.emr.controller;

import com.emr.dto.TestResultCreateRequest;
import com.emr.dto.TestResultResponse;
import com.emr.service.TestResultService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-results")
public class TestResultsController {
  private final TestResultService testResultService;

  public TestResultsController(TestResultService testResultService) {
    this.testResultService = testResultService;
  }

  @GetMapping("/my")
  @PreAuthorize("hasRole('PATIENT')")
  public List<TestResultResponse> myResults() {
    return testResultService.listMyResults();
  }

  @GetMapping("/patient/{patientId}")
  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','ADMIN','LABTECH')")
  public List<TestResultResponse> forPatient(@PathVariable Long patientId) {
    return testResultService.listForPatient(patientId);
  }

  @PostMapping("/patient/{patientId}")
  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','ADMIN','LABTECH')")
  public TestResultResponse add(@PathVariable Long patientId, @Valid @RequestBody TestResultCreateRequest request) {
    return testResultService.add(patientId, request);
  }
}
