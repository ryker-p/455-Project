package com.emr.service;

import com.emr.dto.PatientProfileResponse;
import com.emr.dto.PatientProfileUpdateRequest;
import com.emr.dto.PatientSearchResponse;
import com.emr.exception.ApiException;
import com.emr.model.AccountInfo;
import com.emr.model.Patient;
import com.emr.model.User;
import com.emr.repository.AccountInfoRepository;
import com.emr.repository.InsuranceRepository;
import com.emr.repository.PatientRepository;
import com.emr.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientService {
  private final PatientRepository patientRepository;
  private final AccountInfoRepository accountInfoRepository;
  private final InsuranceRepository insuranceRepository;
  private final CurrentUser currentUser;
  private final AccessLogService accessLogService;

  public PatientService(
      PatientRepository patientRepository,
      AccountInfoRepository accountInfoRepository,
      InsuranceRepository insuranceRepository,
      CurrentUser currentUser,
      AccessLogService accessLogService
  ) {
    this.patientRepository = patientRepository;
    this.accountInfoRepository = accountInfoRepository;
    this.insuranceRepository = insuranceRepository;
    this.currentUser = currentUser;
    this.accessLogService = accessLogService;
  }

  public PatientProfileResponse getMyProfile() {
    User user = currentUser.requireUser();
    Patient patient = patientRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Patient profile not found"));
    accessLogService.log("READ", "Patient", String.valueOf(patient.getId()));
    return toProfile(patient);
  }

  @Transactional
  public PatientProfileResponse updateMyProfile(PatientProfileUpdateRequest request) {
    User user = currentUser.requireUser();
    Patient patient = patientRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Patient profile not found"));

    AccountInfo info = accountInfoRepository.findByUserId(user.getId()).orElseGet(() -> {
      AccountInfo ai = new AccountInfo();
      ai.setUser(user);
      return ai;
    });

    info.setPhone(request.phone());
    info.setAddressLine1(request.addressLine1());
    info.setAddressLine2(request.addressLine2());
    info.setCity(request.city());
    info.setState(request.state());
    info.setZip(request.zip());
    info.setEmergencyContactName(request.emergencyContactName());
    info.setEmergencyContactPhone(request.emergencyContactPhone());
    accountInfoRepository.save(info);

    accessLogService.log("UPDATE", "Patient", String.valueOf(patient.getId()));
    return toProfile(patient);
  }

  public PatientProfileResponse getById(Long patientId) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found"));
    accessLogService.log("READ", "Patient", String.valueOf(patientId));
    return toProfile(patient);
  }

  public List<PatientSearchResponse> search(String q, LocalDate dob, Long patientId) {
    String logKey = (patientId != null ? "id=" + patientId : "") + (dob != null ? " dob=" + dob : "") + (q != null ? " q=" + q : "");
    accessLogService.log("SEARCH", "Patient", logKey.trim());
    String qq = (q == null || q.isBlank()) ? null : q.trim();
    return patientRepository.searchAdvanced(qq, dob, patientId).stream()
        .map(p -> new PatientSearchResponse(p.getId(), p.getFirstName(), p.getLastName(), p.getDateOfBirth(), p.getUser().getEmail()))
        .toList();
  }

  private PatientProfileResponse toProfile(Patient patient) {
    User user = patient.getUser();
    AccountInfo info = accountInfoRepository.findByUserId(user.getId()).orElse(null);
    Long insuranceId = insuranceRepository.findByPatientId(patient.getId()).stream().findFirst().map(i -> i.getId()).orElse(null);
    return new PatientProfileResponse(
        patient.getId(),
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        patient.getFirstName(),
        patient.getLastName(),
        maskSsn(patient.getSsn()),
        patient.getDateOfBirth(),
        patient.getSex(),
        info == null ? null : info.getPhone(),
        info == null ? null : info.getAddressLine1(),
        info == null ? null : info.getAddressLine2(),
        info == null ? null : info.getCity(),
        info == null ? null : info.getState(),
        info == null ? null : info.getZip(),
        insuranceId,
        info == null ? null : info.getEmergencyContactName(),
        info == null ? null : info.getEmergencyContactPhone()
    );
  }

  private String maskSsn(String ssn) {
    if (ssn == null) return null;
    String digits = ssn.replaceAll("[^0-9]", "");
    if (digits.length() < 4) return "***-**-" + "*".repeat(Math.max(0, digits.length()));
    String last4 = digits.substring(digits.length() - 4);
    return "***-**-" + last4;
  }
}
