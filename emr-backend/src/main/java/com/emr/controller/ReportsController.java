package com.emr.controller;

import com.emr.dto.ReportSummaryResponse;
import com.emr.dto.AccessLogReportRow;
import com.emr.dto.BillingStatusReportRow;
import com.emr.dto.DoctorAppointmentReportRow;
import com.emr.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {
  private final ReportService reportService;

  public ReportsController(ReportService reportService) {
    this.reportService = reportService;
  }

  @GetMapping("/summary")
  @PreAuthorize("hasRole('ADMIN')")
  public ReportSummaryResponse summary() {
    return reportService.summary();
  }

  @GetMapping("/appointments-by-doctor")
  @PreAuthorize("hasRole('ADMIN')")
  public List<DoctorAppointmentReportRow> appointmentsByDoctor() {
    return reportService.appointmentsByDoctor();
  }

  @GetMapping("/billing-status")
  @PreAuthorize("hasRole('ADMIN')")
  public List<BillingStatusReportRow> billingStatus() {
    return reportService.billingStatus();
  }

  @GetMapping("/access-log-actions")
  @PreAuthorize("hasRole('ADMIN')")
  public List<AccessLogReportRow> accessLogActions() {
    return reportService.accessLogActions();
  }
}
