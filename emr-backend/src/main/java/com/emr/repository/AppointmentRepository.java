package com.emr.repository;

import com.emr.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
  List<Appointment> findByPatientUserIdOrderByScheduledAtDesc(Long userId);
  List<Appointment> findByDoctorUserIdOrderByScheduledAtDesc(Long userId);

  boolean existsByDoctorIdAndScheduledAtAndStatusNotIn(Long doctorId, java.time.Instant scheduledAt, java.util.List<String> statuses);
  boolean existsByDoctorIdAndScheduledAtAndStatusNotInAndIdNot(
      Long doctorId,
      java.time.Instant scheduledAt,
      java.util.List<String> statuses,
      Long appointmentId
  );
}
