package com.emr.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "Test_Result")
public class TestResult {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "patient_id", nullable = false)
  private Patient patient;

  @ManyToOne
  @JoinColumn(name = "doctor_id")
  private Doctor doctor;

  @Column(name = "test_name", nullable = false, length = 120)
  private String testName;

  @Column(name = "result_value", nullable = false, length = 120)
  private String resultValue;

  @Column(name = "units", length = 40)
  private String units;

  @Column(name = "normal_range", length = 60)
  private String normalRange;

  @Column(name = "result_date")
  private LocalDate resultDate;

  @Column(name = "notes", length = 500)
  private String notes;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public Long getId() {
    return id;
  }

  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public Doctor getDoctor() {
    return doctor;
  }

  public void setDoctor(Doctor doctor) {
    this.doctor = doctor;
  }

  public String getTestName() {
    return testName;
  }

  public void setTestName(String testName) {
    this.testName = testName;
  }

  public String getResultValue() {
    return resultValue;
  }

  public void setResultValue(String resultValue) {
    this.resultValue = resultValue;
  }

  public String getUnits() {
    return units;
  }

  public void setUnits(String units) {
    this.units = units;
  }

  public String getNormalRange() {
    return normalRange;
  }

  public void setNormalRange(String normalRange) {
    this.normalRange = normalRange;
  }

  public LocalDate getResultDate() {
    return resultDate;
  }

  public void setResultDate(LocalDate resultDate) {
    this.resultDate = resultDate;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}

