package com.emr.controller;

import com.emr.dto.AppointmentCreateRequest;
import com.emr.dto.AppointmentResponse;
import com.emr.dto.AppointmentStatusUpdateRequest;
import com.emr.dto.AppointmentUpdateRequest;
import com.emr.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {
  private final AppointmentService appointmentService;

  public AppointmentsController(AppointmentService appointmentService) {
    this.appointmentService = appointmentService;
  }

  @GetMapping("/my")
  public List<AppointmentResponse> myAppointments() {
    return appointmentService.listMyAppointments();
  }

  @PostMapping
  @PreAuthorize("hasRole('PATIENT')")
  public AppointmentResponse create(@Valid @RequestBody AppointmentCreateRequest request) {
    return appointmentService.createForCurrentPatient(request);
  }

  @PutMapping("/{appointmentId}/status")
  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','ADMIN')")
  public AppointmentResponse updateStatus(
      @PathVariable Long appointmentId,
      @Valid @RequestBody AppointmentStatusUpdateRequest request
  ) {
    return appointmentService.updateStatus(appointmentId, request);
  }

  @PutMapping("/{appointmentId}")
  @PreAuthorize("hasAnyRole('DOCTOR','NURSE','ADMIN')")
  public AppointmentResponse update(
      @PathVariable Long appointmentId,
      @Valid @RequestBody AppointmentUpdateRequest request
  ) {
    return appointmentService.updateAppointment(appointmentId, request);
  }
}
