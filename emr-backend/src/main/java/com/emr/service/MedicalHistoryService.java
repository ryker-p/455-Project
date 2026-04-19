package com.emr.service;

import com.emr.dto.MedicalHistoryCreateRequest;
import com.emr.dto.MedicalHistoryResponse;
import com.emr.exception.ApiException;
import com.emr.model.Doctor;
import com.emr.model.MedicalHistory;
import com.emr.model.Patient;
import com.emr.model.Role;
import com.emr.model.User;
import com.emr.repository.DoctorRepository;
import com.emr.repository.MedicalHistoryRepository;
import com.emr.repository.PatientRepository;
import com.emr.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicalHistoryService {
  private final MedicalHistoryRepository medicalHistoryRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final CurrentUser currentUser;
  private final AccessLogService accessLogService;

  public MedicalHistoryService(
      MedicalHistoryRepository medicalHistoryRepository,
      PatientRepository patientRepository,
      DoctorRepository doctorRepository,
      CurrentUser currentUser,
      AccessLogService accessLogService
  ) {
    this.medicalHistoryRepository = medicalHistoryRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    this.currentUser = currentUser;
    this.accessLogService = accessLogService;
  }

  public List<MedicalHistoryResponse> listMyHistory() {
    User user = currentUser.requireUser();
    accessLogService.log("LIST", "MedicalHistory", "my");
    return medicalHistoryRepository.findByPatientUserIdOrderByRecordedAtDesc(user.getId()).stream().map(this::toResponse).toList();
  }

  public List<MedicalHistoryResponse> listForPatient(Long patientId) {
    accessLogService.log("LIST", "MedicalHistory", String.valueOf(patientId));
    return medicalHistoryRepository.findByPatientIdOrderByRecordedAtDesc(patientId).stream().map(this::toResponse).toList();
  }

  @Transactional
  public MedicalHistoryResponse add(Long patientId, MedicalHistoryCreateRequest request) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found"));

    User actor = currentUser.requireUser();
    Doctor doctor = null;
    if (actor.getRole() == Role.DOCTOR) {
      doctor = doctorRepository.findByUserId(actor.getId()).orElse(null);
    }

    MedicalHistory mh = new MedicalHistory();
    mh.setPatient(patient);
    mh.setDoctor(doctor);
    mh.setConditionName(request.conditionName());
    mh.setNotes(request.notes());
    mh = medicalHistoryRepository.save(mh);
    accessLogService.log("CREATE", "MedicalHistory", String.valueOf(mh.getId()));
    return toResponse(mh);
  }

  private MedicalHistoryResponse toResponse(MedicalHistory m) {
    return new MedicalHistoryResponse(
        m.getId(),
        m.getPatient().getId(),
        m.getDoctor() == null ? null : m.getDoctor().getId(),
        m.getConditionName(),
        m.getNotes(),
        m.getRecordedAt()
    );
  }
}

