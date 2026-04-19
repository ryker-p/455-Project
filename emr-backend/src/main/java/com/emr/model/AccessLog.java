package com.emr.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "Access_Log")
public class AccessLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "actor_user_id", nullable = false)
  private User actor;

  @Column(name = "action", nullable = false, length = 80)
  private String action;

  @Column(name = "resource_type", nullable = false, length = 60)
  private String resourceType;

  @Column(name = "resource_id", length = 60)
  private String resourceId;

  @Column(name = "ip_address", length = 60)
  private String ipAddress;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public Long getId() {
    return id;
  }

  public User getActor() {
    return actor;
  }

  public void setActor(User actor) {
    this.actor = actor;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}

