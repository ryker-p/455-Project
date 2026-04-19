package com.emr.service;

import com.emr.dto.DoctorListResponse;
import com.emr.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {
  private final DoctorRepository doctorRepository;
  private final AccessLogService accessLogService;

  public DoctorService(DoctorRepository doctorRepository, AccessLogService accessLogService) {
    this.doctorRepository = doctorRepository;
    this.accessLogService = accessLogService;
  }

  public List<DoctorListResponse> listDoctors() {
    accessLogService.log("LIST", "Doctor", "all");
    return doctorRepository.findAll().stream()
        .map(d -> new DoctorListResponse(d.getId(), d.getFirstName(), d.getLastName(), d.getSpecialty()))
        .toList();
  }
}

