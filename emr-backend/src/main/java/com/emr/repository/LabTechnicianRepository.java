package com.emr.repository;

import com.emr.model.LabTechnician;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabTechnicianRepository extends JpaRepository<LabTechnician, Long> {
  Optional<LabTechnician> findByUserId(Long userId);
}

