package com.emr.service;

import com.emr.dto.TestResultCreateRequest;
import com.emr.dto.TestResultResponse;
import com.emr.exception.ApiException;
import com.emr.model.Doctor;
import com.emr.model.Patient;
import com.emr.model.Role;
import com.emr.model.TestResult;
import com.emr.model.User;
import com.emr.repository.DoctorRepository;
import com.emr.repository.PatientRepository;
import com.emr.repository.TestResultRepository;
import com.emr.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultService {
  private final TestResultRepository testResultRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final CurrentUser currentUser;
  private final AccessLogService accessLogService;

  public TestResultService(
      TestResultRepository testResultRepository,
      PatientRepository patientRepository,
      DoctorRepository doctorRepository,
      CurrentUser currentUser,
      AccessLogService accessLogService
  ) {
    this.testResultRepository = testResultRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    this.currentUser = currentUser;
    this.accessLogService = accessLogService;
  }

  public List<TestResultResponse> listMyResults() {
    User user = currentUser.requireUser();
    accessLogService.log("LIST", "TestResult", "my");
    return testResultRepository.findByPatientUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList();
  }

  public List<TestResultResponse> listForPatient(Long patientId) {
    accessLogService.log("LIST", "TestResult", String.valueOf(patientId));
    return testResultRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream().map(this::toResponse).toList();
  }

  @Transactional
  public TestResultResponse add(Long patientId, TestResultCreateRequest request) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Patient not found"));

    User actor = currentUser.requireUser();
    Doctor doctor = null;
    if (actor.getRole() == Role.DOCTOR) {
      doctor = doctorRepository.findByUserId(actor.getId()).orElse(null);
    }

    TestResult tr = new TestResult();
    tr.setPatient(patient);
    tr.setDoctor(doctor);
    tr.setTestName(request.testName());
    tr.setResultValue(request.resultValue());
    tr.setUnits(request.units());
    tr.setNormalRange(request.normalRange());
    tr.setResultDate(request.resultDate());
    tr.setNotes(request.notes());
    tr = testResultRepository.save(tr);
    accessLogService.log("CREATE", "TestResult", String.valueOf(tr.getId()));
    return toResponse(tr);
  }

  private TestResultResponse toResponse(TestResult t) {
    return new TestResultResponse(
        t.getId(),
        t.getPatient().getId(),
        t.getDoctor() == null ? null : t.getDoctor().getId(),
        t.getTestName(),
        t.getResultValue(),
        t.getUnits(),
        t.getNormalRange(),
        t.getResultDate(),
        t.getNotes(),
        t.getCreatedAt()
    );
  }
}

