package com.emr.security;

import com.emr.exception.ApiException;
import com.emr.model.Role;
import com.emr.model.User;
import com.emr.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
  private final UserRepository userRepository;

  public CurrentUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User requireUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }
    return userRepository.findByUsernameIgnoreCase(auth.getName())
        .or(() -> userRepository.findByEmailIgnoreCase(auth.getName()))
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
  }

  public Role requireRole() {
    return requireUser().getRole();
  }
}
