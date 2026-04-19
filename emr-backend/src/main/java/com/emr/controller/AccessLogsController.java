package com.emr.controller;

import com.emr.dto.AccessLogResponse;
import com.emr.service.AccessLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/access-logs")
public class AccessLogsController {
  private final AccessLogService accessLogService;

  public AccessLogsController(AccessLogService accessLogService) {
    this.accessLogService = accessLogService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<AccessLogResponse> list() {
    return accessLogService.listRecent(250);
  }
}

