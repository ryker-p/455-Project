package com.emr.repository;

import com.emr.model.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InsuranceRepository extends JpaRepository<Insurance, Long> {
  List<Insurance> findByPatientUserId(Long userId);
  List<Insurance> findByPatientId(Long patientId);
}

