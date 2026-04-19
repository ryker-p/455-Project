package com.emr.service;

import com.emr.dto.BillingCreateRequest;
import com.emr.dto.BillingResponse;
import com.emr.dto.BillingStatusUpdateRequest;
import com.emr.exception.ApiException;
import com.emr.model.Appointment;
import com.emr.model.Billing;
import com.emr.model.Patient;
import com.emr.model.User;
import com.emr.repository.AppointmentRepository;
import com.emr.repository.BillingRepository;
import com.emr.repository.PatientRepository;
import com.emr.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class BillingService {
  private static final Set<String> ALLOWED_STATUSES = Set.of("OPEN", "PAID", "VOID");

  private final BillingRepository billingRepository;
  private final PatientRepository patientRepository;
  private final AppointmentRepository appointmentRepository;
  private final CurrentUser currentUser;
  private final AccessLogService accessLogService;

  public BillingService(
      BillingRepository billingRepository,
      PatientRepository patientRepository,
      AppointmentRepository appointmentRepository,
      CurrentUser currentUser,
      AccessLogService accessLogService
  ) {
    this.billingRepository = billingRepository;
    this.patientRepository = patientRepository;
    this.appointmentRepository = appointmentRepository;
    this.currentUser = currentUser;
    this.accessLogService = accessLogService;
  }

  public List<BillingResponse> listMyBilling() {
    User user = currentUser.requireUser();
    accessLogService.log("LIST", "Billing", "my");
    return billingRepository.findByPatientUserIdOrderByCreatedAtDesc(user.getId())
        .stream()
        .map(this::toResponse)
        .toList();
  }

  public List<BillingResponse> listForPatient(Long patientId) {
    accessLogService.log("LIST", "Billing", String.valueOf(patientId));
    return billingRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public BillingResponse create(BillingCreateRequest request) {
    Patient patient = patientRepository.findById(request.patientId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found"));

    Appointment appointment = null;
    if (request.appointmentId() != null) {
      appointment = appointmentRepository.findById(request.appointmentId())
          .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    Billing bill = new Billing();
    bill.setPatient(patient);
    bill.setAppointment(appointment);
    bill.setAmount(request.amount());
    bill.setDueDate(request.dueDate());
    bill.setDescription(request.description());
    bill.setStatus("OPEN");
    bill = billingRepository.save(bill);
    accessLogService.log("CREATE", "Billing", String.valueOf(bill.getId()));
    return toResponse(bill);
  }

  @Transactional
  public BillingResponse updateStatus(Long billingId, BillingStatusUpdateRequest request) {
    if (!ALLOWED_STATUSES.contains(request.status().toUpperCase())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status");
    }
    Billing bill = billingRepository.findById(billingId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Billing record not found"));
    bill.setStatus(request.status().toUpperCase());
    accessLogService.log("UPDATE_STATUS", "Billing", String.valueOf(billingId));
    return toResponse(bill);
  }

  private BillingResponse toResponse(Billing b) {
    return new BillingResponse(
        b.getId(),
        b.getPatient().getId(),
        b.getAppointment() == null ? null : b.getAppointment().getId(),
        b.getAmount(),
        b.getStatus(),
        b.getDueDate(),
        b.getDescription(),
        b.getCreatedAt()
    );
  }
}

