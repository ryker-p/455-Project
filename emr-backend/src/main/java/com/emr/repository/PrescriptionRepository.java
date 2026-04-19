package com.emr.repository;

import com.emr.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
  List<Prescription> findByPatientUserIdOrderByCreatedAtDesc(Long userId);
  List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId);
  long count();
}

