package com.emr.service;

import com.emr.dto.ReportSummaryResponse;
import com.emr.dto.AccessLogReportRow;
import com.emr.dto.BillingStatusReportRow;
import com.emr.dto.DoctorAppointmentReportRow;
import com.emr.model.Appointment;
import com.emr.repository.AppointmentRepository;
import com.emr.repository.AccessLogRepository;
import com.emr.repository.BillingRepository;
import com.emr.repository.DoctorRepository;
import com.emr.repository.PatientRepository;
import com.emr.repository.PrescriptionRepository;
import com.emr.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
  private final UserRepository userRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final AppointmentRepository appointmentRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final BillingRepository billingRepository;
  private final AccessLogRepository accessLogRepository;

  public ReportService(
      UserRepository userRepository,
      PatientRepository patientRepository,
      DoctorRepository doctorRepository,
      AppointmentRepository appointmentRepository,
      PrescriptionRepository prescriptionRepository,
      BillingRepository billingRepository,
      AccessLogRepository accessLogRepository
  ) {
    this.userRepository = userRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    this.appointmentRepository = appointmentRepository;
    this.prescriptionRepository = prescriptionRepository;
    this.billingRepository = billingRepository;
    this.accessLogRepository = accessLogRepository;
  }

  public ReportSummaryResponse summary() {
    return new ReportSummaryResponse(
        userRepository.count(),
        patientRepository.count(),
        doctorRepository.count(),
        appointmentRepository.count(),
        prescriptionRepository.count(),
        billingRepository.countByStatus("OPEN")
    );
  }

  public List<DoctorAppointmentReportRow> appointmentsByDoctor() {
    List<Appointment> appts = appointmentRepository.findAll();
    Map<Long, List<Appointment>> byDoctor = appts.stream().collect(Collectors.groupingBy(a -> a.getDoctor().getId()));
    return byDoctor.entrySet().stream()
        .map(e -> {
          Long doctorId = e.getKey();
          List<Appointment> rows = e.getValue();
          String doctorName = rows.get(0).getDoctor().getFirstName() + " " + rows.get(0).getDoctor().getLastName();
          long scheduled = rows.stream().filter(a -> "SCHEDULED".equalsIgnoreCase(a.getStatus())).count();
          long confirmed = rows.stream().filter(a -> "CONFIRMED".equalsIgnoreCase(a.getStatus())).count();
          long completed = rows.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
          long cancelled = rows.stream().filter(a -> "CANCELLED".equalsIgnoreCase(a.getStatus())).count();
          return new DoctorAppointmentReportRow(doctorId, doctorName, rows.size(), scheduled, confirmed, completed, cancelled);
        })
        .sorted(Comparator.comparingLong(DoctorAppointmentReportRow::total).reversed())
        .toList();
  }

  public List<BillingStatusReportRow> billingStatus() {
    return billingRepository.countByStatusGroup().stream()
        .map(r -> new BillingStatusReportRow((String) r[0], (Long) r[1]))
        .toList();
  }

  public List<AccessLogReportRow> accessLogActions() {
    return accessLogRepository.countByAction().stream()
        .map(r -> new AccessLogReportRow((String) r[0], (Long) r[1]))
        .toList();
  }
}
