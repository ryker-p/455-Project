package com.emr.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "Medical_History")
public class MedicalHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "patient_id", nullable = false)
  private Patient patient;

  @ManyToOne
  @JoinColumn(name = "doctor_id")
  private Doctor doctor;

  @Column(name = "condition_name", nullable = false, length = 120)
  private String conditionName;

  @Column(name = "notes", length = 500)
  private String notes;

  @Column(name = "recorded_at", nullable = false)
  private Instant recordedAt = Instant.now();

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

  public String getConditionName() {
    return conditionName;
  }

  public void setConditionName(String conditionName) {
    this.conditionName = conditionName;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }
}

