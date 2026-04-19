package com.emr.service;

import com.emr.dto.AdminCreateUserRequest;
import com.emr.dto.MeResponse;
import com.emr.dto.RoleUpdateRequest;
import com.emr.dto.TwoFactorSetupResponse;
import com.emr.dto.TwoFactorUpdateRequest;
import com.emr.dto.UserListResponse;
import com.emr.exception.ApiException;
import com.emr.model.*;
import com.emr.repository.*;
import com.emr.security.TotpService;
import com.emr.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final PatientRepository patientRepository;
  private final DoctorRepository doctorRepository;
  private final NurseRepository nurseRepository;
  private final LabTechnicianRepository labTechnicianRepository;
  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;
  private final CurrentUser currentUser;
  private final AccessLogService accessLogService;
  private final TotpService totpService;

  public UserService(
      UserRepository userRepository,
      PatientRepository patientRepository,
      DoctorRepository doctorRepository,
      NurseRepository nurseRepository,
      LabTechnicianRepository labTechnicianRepository,
      AdminRepository adminRepository,
      PasswordEncoder passwordEncoder,
      CurrentUser currentUser,
      AccessLogService accessLogService,
      TotpService totpService
  ) {
    this.userRepository = userRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    this.nurseRepository = nurseRepository;
    this.labTechnicianRepository = labTechnicianRepository;
    this.adminRepository = adminRepository;
    this.passwordEncoder = passwordEncoder;
    this.currentUser = currentUser;
    this.accessLogService = accessLogService;
    this.totpService = totpService;
  }

  public MeResponse me() {
    return meFor(currentUser.requireUser());
  }

  public MeResponse meFor(User user) {
    Long patientId = patientRepository.findByUserId(user.getId()).map(Patient::getId).orElse(null);
    Long doctorId = doctorRepository.findByUserId(user.getId()).map(Doctor::getId).orElse(null);
    Long nurseId = nurseRepository.findByUserId(user.getId()).map(Nurse::getId).orElse(null);
    Long labTechId = labTechnicianRepository.findByUserId(user.getId()).map(LabTechnician::getId).orElse(null);
    Long adminId = adminRepository.findByUserId(user.getId()).map(Admin::getId).orElse(null);
    return new MeResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole().name(),
        patientId,
        doctorId,
        nurseId,
        labTechId,
        adminId,
        user.getDisplayName(),
        user.isTwoFactorEnabled()
    );
  }

  public List<UserListResponse> listUsers() {
    accessLogService.log("LIST", "User", "all");
    return userRepository.findAll().stream().map(this::toListResponse).toList();
  }

  @Transactional
  public UserListResponse adminCreateUser(AdminCreateUserRequest request) {
    if (userRepository.existsByEmailIgnoreCase(request.email())) {
      throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
    }
    if (userRepository.existsByUsernameIgnoreCase(request.username())) {
      throw new ApiException(HttpStatus.CONFLICT, "Username already in use");
    }
    Role role = parseRole(request.role());

    User u = new User();
    u.setUsername(request.username().trim());
    u.setEmail(request.email().trim().toLowerCase());
    u.setPasswordHash(passwordEncoder.encode(request.password()));
    u.setRole(role);
    u.setDisplayName(request.firstName().trim() + " " + request.lastName().trim());
    u = userRepository.save(u);

    if (role == Role.PATIENT) {
      Patient p = new Patient();
      p.setUser(u);
      p.setFirstName(request.firstName().trim());
      p.setLastName(request.lastName().trim());
      patientRepository.save(p);
    } else if (role == Role.DOCTOR) {
      Doctor d = new Doctor();
      d.setUser(u);
      d.setFirstName(request.firstName().trim());
      d.setLastName(request.lastName().trim());
      doctorRepository.save(d);
    } else if (role == Role.NURSE) {
      Nurse n = new Nurse();
      n.setUser(u);
      n.setFirstName(request.firstName().trim());
      n.setLastName(request.lastName().trim());
      nurseRepository.save(n);
    } else if (role == Role.LABTECH) {
      LabTechnician t = new LabTechnician();
      t.setUser(u);
      t.setFirstName(request.firstName().trim());
      t.setLastName(request.lastName().trim());
      labTechnicianRepository.save(t);
    } else if (role == Role.ADMIN) {
      Admin a = new Admin();
      a.setUser(u);
      a.setTitle("System Admin");
      adminRepository.save(a);
    }

    accessLogService.log("CREATE", "User", String.valueOf(u.getId()));
    return toListResponse(u);
  }

  @Transactional
  public UserListResponse updateRole(Long userId, RoleUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    Role role = parseRole(request.role());
    user.setRole(role);
    accessLogService.log("UPDATE_ROLE", "User", String.valueOf(userId));
    return toListResponse(user);
  }

  @Transactional
  public TwoFactorSetupResponse updateTwoFactor(Long userId, TwoFactorUpdateRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

    if (request.enabled()) {
      if (user.getTwoFactorSecret() == null || user.getTwoFactorSecret().isBlank()) {
        user.setTwoFactorSecret(totpService.generateBase32Secret());
      }
      user.setTwoFactorEnabled(true);
      accessLogService.log("ENABLE_2FA", "User", String.valueOf(userId));
      return new TwoFactorSetupResponse(
          user.getId(),
          true,
          user.getTwoFactorSecret(),
          totpService.buildOtpAuthUri(user.getUsername(), user.getTwoFactorSecret())
      );
    } else {
      user.setTwoFactorEnabled(false);
      accessLogService.log("DISABLE_2FA", "User", String.valueOf(userId));
      return new TwoFactorSetupResponse(user.getId(), false, null, null);
    }
  }

  private UserListResponse toListResponse(User u) {
    return new UserListResponse(
        u.getId(),
        u.getUsername(),
        u.getEmail(),
        u.getRole().name(),
        u.isEnabled(),
        u.getCreatedAt(),
        u.getDisplayName(),
        u.isTwoFactorEnabled()
    );
  }

  private Role parseRole(String raw) {
    try {
      return Role.valueOf(raw.trim().toUpperCase());
    } catch (Exception ex) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid role");
    }
  }
}
