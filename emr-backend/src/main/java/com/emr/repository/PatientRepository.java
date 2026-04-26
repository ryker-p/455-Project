package com.emr.repository;

import com.emr.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
  Optional<Patient> findByUserId(Long userId);

  @Query("""
      select p from Patient p
      where lower(p.firstName) like lower(concat('%', :q, '%'))
         or lower(p.lastName) like lower(concat('%', :q, '%'))
         or lower(p.user.email) like lower(concat('%', :q, '%'))
      order by p.lastName asc, p.firstName asc
      """)
  List<Patient> search(@Param("q") String q);


}