package com.emr.repository;

import com.emr.model.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
  List<TestResult> findByPatientUserIdOrderByCreatedAtDesc(Long userId);
  List<TestResult> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}

