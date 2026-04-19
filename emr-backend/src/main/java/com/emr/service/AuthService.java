package com.emr.service;

import com.emr.dto.AuthLoginRequest;
import com.emr.dto.AuthResponse;
import com.emr.dto.AuthSignupRequest;
import com.emr.dto.MeResponse;
import com.emr.dto.ResetPasswordRequest;
import com.emr.exception.ApiException;
import com.emr.model.AccessLog;
import com.emr.model.Patient;
import com.emr.model.Role;
import com.emr.model.User;
import com.emr.repository.AccessLogRepository;
import com.emr.repository.PatientRepository;
import com.emr.repository.UserRepository;
import com.emr.security.JwtService;
import com.emr.security.TotpService;
import jakarta.transaction.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PatientRepository patientRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final UserService userService;
  private final TotpService totpService;
  private final AccessLogRepository accessLogRepository;
  private final HttpServletRequest httpServletRequest;

  private static final int MAX_FAILED_ATTEMPTS = 5;
  private static final long LOCKOUT_SECONDS = 15 * 60;

  public AuthService(
      UserRepository userRepository,
      PatientRepository patientRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      UserService userService,
      TotpService totpService,
      AccessLogRepository accessLogRepository,
      HttpServletRequest httpServletRequest
  ) {
    this.userRepository = userRepository;
    this.patientRepository = patientRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.userService = userService;
    this.totpService = totpService;
    this.accessLogRepository = accessLogRepository;
    this.httpServletRequest = httpServletRequest;
  }

  public AuthResponse login(AuthLoginRequest request) {
    String ident = request.identifier().trim();
    User user = userRepository.findByUsernameIgnoreCase(ident)
        .or(() -> userRepository.findByEmailIgnoreCase(ident))
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password"));

    if (!user.isEnabled()) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Account disabled");
    }

    if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
      throw new ApiException(HttpStatus.LOCKED, "Account temporarily locked. Try again later.");
    }

    boolean passwordOk = passwordEncoder.matches(request.password(), user.getPasswordHash());
    if (!passwordOk) {
      int next = user.getFailedLoginAttempts() + 1;
      user.setFailedLoginAttempts(next);
      if (next >= MAX_FAILED_ATTEMPTS) {
        user.setLockedUntil(Instant.now().plusSeconds(LOCKOUT_SECONDS));
        user.setFailedLoginAttempts(0);
      }
      userRepository.save(user);
      logAuth(user, "LOGIN_FAILED");
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password");
    }

    user.setFailedLoginAttempts(0);
    user.setLockedUntil(null);

    if (user.isTwoFactorEnabled()) {
      if (request.twoFactorCode() == null || request.twoFactorCode().isBlank()) {
        throw new ApiException(HttpStatus.PRECONDITION_REQUIRED, "2FA code required");
      }
      if (!totpService.verifyCode(user.getTwoFactorSecret(), request.twoFactorCode())) {
        logAuth(user, "LOGIN_2FA_FAILED");
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid 2FA code");
      }
    }

    user.setLastLoginAt(Instant.now());
    userRepository.save(user);
    logAuth(user, "LOGIN_SUCCESS");

    String token = jwtService.generateToken(user.getUsername(), user.getRole());
    MeResponse me = userService.meFor(user);
    return new AuthResponse(token, me);
  }

  @Transactional
  public AuthResponse registerPatient(AuthSignupRequest request) {
    if (userRepository.existsByUsernameIgnoreCase(request.username())) {
      throw new ApiException(HttpStatus.CONFLICT, "Username already in use");
    }
    if (userRepository.existsByEmailIgnoreCase(request.email())) {
      throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
    }

    User user = new User();
    user.setUsername(request.username().trim());
    user.setEmail(request.email().trim().toLowerCase());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setRole(Role.PATIENT);
    user.setDisplayName(request.firstName().trim() + " " + request.lastName().trim());
    user = userRepository.save(user);

    Patient patient = new Patient();
    patient.setUser(user);
    patient.setFirstName(request.firstName().trim());
    patient.setLastName(request.lastName().trim());
    patientRepository.save(patient);

    String token = jwtService.generateToken(user.getUsername(), user.getRole());
    MeResponse me = userService.meFor(user);
    logAuth(user, "REGISTER");
    return new AuthResponse(token, me);
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    User user = userRepository.findByEmailIgnoreCase(request.email())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No account with that email"));
    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    logAuth(user, "PASSWORD_RESET");
  }

  private void logAuth(User actor, String action) {
    AccessLog log = new AccessLog();
    log.setActor(actor);
    log.setAction(action);
    log.setResourceType("Auth");
    log.setResourceId(actor.getUsername());
    log.setIpAddress(httpServletRequest.getRemoteAddr());
    accessLogRepository.save(log);
  }
}
