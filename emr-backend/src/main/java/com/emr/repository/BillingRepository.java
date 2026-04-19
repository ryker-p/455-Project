package com.emr.repository;

import com.emr.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BillingRepository extends JpaRepository<Billing, Long> {
  List<Billing> findByPatientUserIdOrderByCreatedAtDesc(Long userId);
  List<Billing> findByPatientIdOrderByCreatedAtDesc(Long patientId);
  long countByStatus(String status);

  @Query("select b.status, count(b) from Billing b group by b.status order by count(b) desc")
  List<Object[]> countByStatusGroup();
}
