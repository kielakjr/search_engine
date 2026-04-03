package com.kielakjr.search_engine.auth;

import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtService jwtTokenProvider;

  @InjectMocks
  private AuthService authService;

  private UserRequest userRequest;

  @BeforeEach
  void setUp() {
    userRequest = new UserRequest("test@test.com", "password");
  }

  @Nested
  @DisplayName("register method")
  class RegisterTests {

    @Test
    void regsiterWithExistingUser() {
      when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

      assertThatThrownBy(() -> authService.register(userRequest))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Email already exists");
    }

    @Test
    void registerWithNewUser() {
      when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
      when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
      User newUser = User.builder()
          .email("test@test.com")
          .password("encodedPassword")
          .role(Role.USER)
          .build();
      when(userRepository.save(newUser)).thenReturn(newUser);

      UserResponse result = authService.register(userRequest);
      assertThat(result).isNotNull();
      assertThat(result.getEmail()).isEqualTo("test@test.com");
    }
  }

  @Nested
  @DisplayName("login method")
  class LoginTests {
    @Test
    void loginWithInvalidEmail() {
      when(userRepository.findByEmail("invalid@test.com")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.login(new UserRequest("invalid@test.com", "password")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Invalid email or password");
    }

    @Test
    void loginWithInvalidPassword() {
      User user = User.builder()
          .email("test@test.com")
          .password("encodedPassword")
          .build();
      when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(false);

      assertThatThrownBy(() -> authService.login(userRequest))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Invalid email or password");
    }

    @Test
    void loginWithValidCredentials() {
      User user = User.builder()
          .email("test@test.com")
          .password("encodedPassword")
          .build();
      when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
      when(jwtTokenProvider.generateToken(user)).thenReturn("jwtToken");

      AuthTokenResponse result = authService.login(userRequest);

      assertThat(result).isNotNull();
      assertThat(result.getUser().getEmail()).isEqualTo("test@test.com");
      assertThat(result.getToken()).isEqualTo("jwtToken");
    }
  }
}

