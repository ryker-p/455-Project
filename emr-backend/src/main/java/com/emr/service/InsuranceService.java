package com.emr.service;

import com.emr.dto.InsuranceRequest;
import com.emr.dto.InsuranceResponse;
import com.emr.exception.ApiException;
import com.emr.model.Insurance;
import com.emr.model.Patient;
import com.emr.model.User;
import com.emr.repository.InsuranceRepository;
import com.emr.repository.PatientRepository;
import com.emr.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InsuranceService {
  private final InsuranceRepository insuranceRepository;
  private final PatientRepository patientRepository;
  private final CurrentUser currentUser;
  private final AccessLogService accessLogService;

  public InsuranceService(
      InsuranceRepository insuranceRepository,
      PatientRepository patientRepository,
      CurrentUser currentUser,
      AccessLogService accessLogService
  ) {
    this.insuranceRepository = insuranceRepository;
    this.patientRepository = patientRepository;
    this.currentUser = currentUser;
    this.accessLogService = accessLogService;
  }

  public List<InsuranceResponse> listMyInsurance() {
    User user = currentUser.requireUser();
    accessLogService.log("LIST", "Insurance", "my");
    return insuranceRepository.findByPatientUserId(user.getId()).stream().map(this::toResponse).toList();
  }

  public List<InsuranceResponse> listForPatient(Long patientId) {
    accessLogService.log("LIST", "Insurance", String.valueOf(patientId));
    return insuranceRepository.findByPatientId(patientId).stream().map(this::toResponse).toList();
  }

  @Transactional
  public InsuranceResponse upsert(Long patientId, InsuranceRequest request) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found"));

    Insurance insurance = insuranceRepository.findByPatientId(patientId).stream().findFirst().orElseGet(Insurance::new);
    insurance.setPatient(patient);
    insurance.setProviderName(request.providerName());
    insurance.setPolicyNumber(request.policyNumber());
    insurance.setGroupNumber(request.groupNumber());
    insurance.setEffectiveDate(request.effectiveDate());
    insurance.setExpirationDate(request.expirationDate());
    insurance = insuranceRepository.save(insurance);
    accessLogService.log("UPSERT", "Insurance", String.valueOf(insurance.getId()));
    return toResponse(insurance);
  }

  private InsuranceResponse toResponse(Insurance i) {
    return new InsuranceResponse(
        i.getId(),
        i.getPatient().getId(),
        i.getProviderName(),
        i.getPolicyNumber(),
        i.getGroupNumber(),
        i.getEffectiveDate(),
        i.getExpirationDate()
    );
  }
}

