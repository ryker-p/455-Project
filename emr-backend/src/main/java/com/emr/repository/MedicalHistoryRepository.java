package com.emr.repository;

import com.emr.model.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {
  List<MedicalHistory> findByPatientUserIdOrderByRecordedAtDesc(Long userId);
  List<MedicalHistory> findByPatientIdOrderByRecordedAtDesc(Long patientId);
}

