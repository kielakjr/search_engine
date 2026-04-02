package com.kielakjr.search_engine.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserResponse register(UserRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new IllegalArgumentException("Email already exists");
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.USER)
        .build();

    User savedUser = userRepository.save(user);

    return UserResponse.builder()
        .id(savedUser.getId())
        .username(savedUser.getEmail())
        .role(savedUser.getRole())
        .build();
  }

  public UserResponse login(UserRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new IllegalArgumentException("Invalid email or password");
    }

    return UserResponse.builder()
        .id(user.getId())
        .username(user.getEmail())
        .role(user.getRole())
        .build();
  }
}
