package com.emr.service;

import com.emr.dto.AccessLogResponse;
import com.emr.model.AccessLog;
import com.emr.model.User;
import com.emr.repository.AccessLogRepository;
import com.emr.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccessLogService {
  private final AccessLogRepository accessLogRepository;
  private final CurrentUser currentUser;
  private final HttpServletRequest request;

  public AccessLogService(AccessLogRepository accessLogRepository, CurrentUser currentUser, HttpServletRequest request) {
    this.accessLogRepository = accessLogRepository;
    this.currentUser = currentUser;
    this.request = request;
  }

  public void log(String action, String resourceType, String resourceId) {
    User actor = currentUser.requireUser();
    AccessLog log = new AccessLog();
    log.setActor(actor);
    log.setAction(action);
    log.setResourceType(resourceType);
    log.setResourceId(resourceId);
    log.setIpAddress(request.getRemoteAddr());
    accessLogRepository.save(log);
  }

  public List<AccessLogResponse> listRecent(int limit) {
    return accessLogRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
        .stream()
        .map(l -> new AccessLogResponse(
            l.getId(),
            l.getActor().getId(),
            l.getActor().getEmail(),
            l.getAction(),
            l.getResourceType(),
            l.getResourceId(),
            l.getIpAddress(),
            l.getCreatedAt()
        ))
        .toList();
  }
}

