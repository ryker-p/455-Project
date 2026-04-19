package com.emr.service;

import com.emr.dto.AppointmentCreateRequest;
import com.emr.dto.AppointmentResponse;
import com.emr.dto.AppointmentStatusUpdateRequest;
import com.emr.dto.AppointmentUpdateRequest;
import com.emr.exception.ApiException;
import com.emr.model.*;
import com.emr.repository.AppointmentRepository;
import com.emr.repository.DoctorRepository;
import com.emr.repository.PatientRepository;
import com.emr.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {
  private static final Set<String> ALLOWED_STATUSES = Set.of("SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED", "NO_SHOW");
  private static final List<String> BLOCKING_STATUSES = List.of("CANCELLED", "NO_SHOW");

  private final AppointmentRepository appointmentRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final CurrentUser currentUser;
  private final AccessLogService accessLogService;

  public AppointmentService(
      AppointmentRepository appointmentRepository,
      PatientRepository patientRepository,
      DoctorRepository doctorRepository,
      CurrentUser currentUser,
      AccessLogService accessLogService
  ) {
    this.appointmentRepository = appointmentRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    this.currentUser = currentUser;
    this.accessLogService = accessLogService;
  }

  public List<AppointmentResponse> listMyAppointments() {
    User user = currentUser.requireUser();
    List<Appointment> rows;
    if (user.getRole() == Role.PATIENT) {
      rows = appointmentRepository.findByPatientUserIdOrderByScheduledAtDesc(user.getId());
    } else if (user.getRole() == Role.DOCTOR) {
      rows = appointmentRepository.findByDoctorUserIdOrderByScheduledAtDesc(user.getId());
    } else {
      rows = appointmentRepository.findAll();
    }
    accessLogService.log("LIST", "Appointment", "my");
    return rows.stream().map(this::toResponse).toList();
  }

  @Transactional
  public AppointmentResponse createForCurrentPatient(AppointmentCreateRequest request) {
    User user = currentUser.requireUser();
    Patient patient = patientRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Patient profile not found"));

    Doctor doctor = doctorRepository.findById(request.doctorId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor not found"));

    if (appointmentRepository.existsByDoctorIdAndScheduledAtAndStatusNotIn(doctor.getId(), request.scheduledAt(), BLOCKING_STATUSES)) {
      throw new ApiException(HttpStatus.CONFLICT, "Doctor is already booked at that time");
    }

    Appointment appt = new Appointment();
    appt.setPatient(patient);
    appt.setDoctor(doctor);
    appt.setScheduledAt(request.scheduledAt());
    appt.setReason(request.reason());
    appt.setStatus("SCHEDULED");
    appt = appointmentRepository.save(appt);
    accessLogService.log("CREATE", "Appointment", String.valueOf(appt.getId()));
    return toResponse(appt);
  }

  @Transactional
  public AppointmentResponse updateStatus(Long appointmentId, AppointmentStatusUpdateRequest request) {
    if (!ALLOWED_STATUSES.contains(request.status().toUpperCase())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status");
    }
    Appointment appt = appointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
    appt.setStatus(request.status().toUpperCase());
    appt.setNotes(request.notes());
    accessLogService.log("UPDATE_STATUS", "Appointment", String.valueOf(appointmentId));
    return toResponse(appt);
  }

  @Transactional
  public AppointmentResponse updateAppointment(Long appointmentId, AppointmentUpdateRequest request) {
    Appointment appt = appointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));

    if (request.scheduledAt() != null) {
      if (appointmentRepository.existsByDoctorIdAndScheduledAtAndStatusNotIn(appt.getDoctor().getId(), request.scheduledAt(), BLOCKING_STATUSES)) {
        throw new ApiException(HttpStatus.CONFLICT, "Doctor is already booked at that time");
      }
      appt.setScheduledAt(request.scheduledAt());
    }
    if (request.status() != null && !request.status().isBlank()) {
      if (!ALLOWED_STATUSES.contains(request.status().toUpperCase())) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status");
      }
      appt.setStatus(request.status().toUpperCase());
    }
    if (request.notes() != null) {
      appt.setNotes(request.notes());
    }
    accessLogService.log("UPDATE", "Appointment", String.valueOf(appointmentId));
    return toResponse(appt);
  }

  private AppointmentResponse toResponse(Appointment a) {
    String patientName = a.getPatient().getFirstName() + " " + a.getPatient().getLastName();
    String doctorName = a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName();
    return new AppointmentResponse(
        a.getId(),
        a.getPatient().getId(),
        patientName,
        a.getDoctor().getId(),
        doctorName,
        a.getScheduledAt(),
        a.getStatus(),
        a.getReason(),
        a.getNotes()
    );
  }
}
