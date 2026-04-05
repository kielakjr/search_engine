package com.kielakjr.search_engine.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    Map<String, String> error = new HashMap<>();
    e.getBindingResult().getFieldErrors().forEach(fieldError -> {
      error.put(fieldError.getField(), fieldError.getDefaultMessage());
    });
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
    Map<String, String> error = new HashMap<>();
    error.put("error", "Invalid argument: " + e.getMessage());
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    Map<String, String> error = new HashMap<>();
    error.put("error", "Request body is missing or malformed");
    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleException(Exception e) {
    Map<String, String> error = new HashMap<>();
    error.put("error", "An error occurred: " + e.getMessage());
    return ResponseEntity.status(500).body(error);
  }
}
