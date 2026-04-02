package com.kielakjr.search_engine.auth;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  public UserResponse register(@Valid @RequestBody UserRequest userRequest) {
    return authService.register(userRequest);
  }

  @PostMapping("/login")
  public AuthTokenResponse login(@Valid @RequestBody UserRequest userRequest) {
    return authService.login(userRequest);
  }
}
