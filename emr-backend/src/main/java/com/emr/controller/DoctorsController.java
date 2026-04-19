package com.emr.controller;

import com.emr.dto.DoctorListResponse;
import com.emr.service.DoctorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorsController {
  private final DoctorService doctorService;

  public DoctorsController(DoctorService doctorService) {
    this.doctorService = doctorService;
  }

  @GetMapping
  public List<DoctorListResponse> list() {
    return doctorService.listDoctors();
  }
}

