package com.emr.service;

import com.emr.dto.PrescriptionCreateRequest;
import com.emr.dto.PrescriptionResponse;
import com.emr.dto.PrescriptionStatusUpdateRequest;
import com.emr.exception.ApiException;
import com.emr.model.Doctor;
import com.emr.model.Patient;
import com.emr.model.Prescription;
import com.emr.model.User;
import com.emr.repository.DoctorRepository;
import com.emr.repository.PatientRepository;
import com.emr.repository.PrescriptionRepository;
import com.emr.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PrescriptionService {
  private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "DISCONTINUED", "COMPLETED");

  private final PrescriptionRepository prescriptionRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final CurrentUser currentUser;
  private final AccessLogService accessLogService;

  public PrescriptionService(
      PrescriptionRepository prescriptionRepository,
      PatientRepository patientRepository,
      DoctorRepository doctorRepository,
      CurrentUser currentUser,
      AccessLogService accessLogService
  ) {
    this.prescriptionRepository = prescriptionRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    this.currentUser = currentUser;
    this.accessLogService = accessLogService;
  }

  public List<PrescriptionResponse> listMyPrescriptions() {
    User user = currentUser.requireUser();
    accessLogService.log("LIST", "Prescription", "my");
    return prescriptionRepository.findByPatientUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList();
  }

  public List<PrescriptionResponse> listForPatient(Long patientId) {
    accessLogService.log("LIST", "Prescription", String.valueOf(patientId));
    return prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream().map(this::toResponse).toList();
  }

  @Transactional
  public PrescriptionResponse create(Long patientId, PrescriptionCreateRequest request) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found"));

    User user = currentUser.requireUser();
    Doctor doctor = doctorRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Doctor profile not found"));

    Prescription p = new Prescription();
    p.setPatient(patient);
    p.setDoctor(doctor);
    p.setMedicationName(request.medicationName());
    p.setDosage(request.dosage());
    p.setInstructions(request.instructions());
    p.setStartDate(request.startDate());
    p.setEndDate(request.endDate());
    p.setStatus("ACTIVE");
    p = prescriptionRepository.save(p);
    accessLogService.log("CREATE", "Prescription", String.valueOf(p.getId()));
    return toResponse(p);
  }

  @Transactional
  public PrescriptionResponse updateStatus(Long prescriptionId, PrescriptionStatusUpdateRequest request) {
    if (!ALLOWED_STATUSES.contains(request.status().toUpperCase())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status");
    }
    Prescription p = prescriptionRepository.findById(prescriptionId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prescription not found"));
    p.setStatus(request.status().toUpperCase());
    accessLogService.log("UPDATE_STATUS", "Prescription", String.valueOf(prescriptionId));
    return toResponse(p);
  }

  private PrescriptionResponse toResponse(Prescription p) {
    return new PrescriptionResponse(
        p.getId(),
        p.getPatient().getId(),
        p.getDoctor().getId(),
        p.getMedicationName(),
        p.getDosage(),
        p.getInstructions(),
        p.getStartDate(),
        p.getEndDate(),
        p.getStatus(),
        p.getCreatedAt()
    );
  }
}

