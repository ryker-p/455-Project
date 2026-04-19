package com.emr.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<?> apiException(ApiException ex) {
    return ResponseEntity.status(ex.getStatus()).body(error(ex.getStatus(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest().body(error(HttpStatus.BAD_REQUEST, "Validation failed"));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<?> constraint(ConstraintViolationException ex) {
    return ResponseEntity.badRequest().body(error(HttpStatus.BAD_REQUEST, "Validation failed"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> other(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error"));
  }

  private Map<String, Object> error(HttpStatus status, String message) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("timestamp", Instant.now().toString());
    payload.put("status", status.value());
    payload.put("error", status.getReasonPhrase());
    payload.put("message", message);
    return payload;
  }
}

