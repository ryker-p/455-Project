package com.emr.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Lab_Technician")
public class LabTechnician {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "first_name", nullable = false, length = 60)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 60)
  private String lastName;

  @Column(name = "ssn", length = 11, unique = true)
  private String ssn;

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getSsn() {
    return ssn;
  }

  public void setSsn(String ssn) {
    this.ssn = ssn;
  }
}

