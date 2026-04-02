package com.kielakjr.search_engine.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class AuthTokenResponse {
  private String token;
  private UserResponse user;
}
