package com.emr.controller;

import com.emr.dto.*;
import com.emr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {
  private final UserService userService;

  public UsersController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public MeResponse me() {
    return userService.me();
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserListResponse> list() {
    return userService.listUsers();
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public UserListResponse createUser(@Valid @RequestBody AdminCreateUserRequest request) {
    return userService.adminCreateUser(request);
  }

  @PutMapping("/{userId}/role")
  @PreAuthorize("hasRole('ADMIN')")
  public UserListResponse updateRole(@PathVariable Long userId, @Valid @RequestBody RoleUpdateRequest request) {
    return userService.updateRole(userId, request);
  }

  @PutMapping("/{userId}/2fa")
  @PreAuthorize("hasRole('ADMIN')")
  public TwoFactorSetupResponse updateTwoFactor(@PathVariable Long userId, @RequestBody TwoFactorUpdateRequest request) {
    return userService.updateTwoFactor(userId, request);
  }
}
