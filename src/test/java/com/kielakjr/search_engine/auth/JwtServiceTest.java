package com.kielakjr.search_engine.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kielakjr.search_engine.config.JwtProperties;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

  @Mock
  private JwtProperties jwtProperties;

  @InjectMocks
  private JwtService jwtService;

  private User testUser;

  private static final String SECRET = "dGVzdFNlY3JldEtleUZvckp3dFRlc3RpbmdQdXJwb3Nlcw==";
  private static final long EXPIRATION_MS = 3_600_000L;

  @BeforeEach
  void setUp() {
    when(jwtProperties.getSecret()).thenReturn(SECRET);

    testUser = User.builder()
        .email("test@test.com")
        .password("encodedPassword")
        .role(Role.USER)
        .build();
  }

  @Nested
  @DisplayName("generateToken method")
  class GenerateTokenTests {

    @Test
    void generateTokenReturnsNonNullToken() {
      when(jwtProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
      String token = jwtService.generateToken(testUser);
      assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateTokenContainsCorrectEmail() {
      when(jwtProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
      String token = jwtService.generateToken(testUser);
      assertThat(jwtService.extractEmail(token)).isEqualTo("test@test.com");
    }
  }

  @Nested
  @DisplayName("validateToken method")
  class ValidateTokenTests {

    @Test
    void validateTokenReturnsTrueForValidToken() {
      when(jwtProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
      String token = jwtService.generateToken(testUser);
      assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    void validateTokenReturnsFalseForTamperedToken() {
      when(jwtProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
      String token = jwtService.generateToken(testUser) + "tampered";
      assertThat(jwtService.validateToken(token)).isFalse();
    }

    @Test
    void validateTokenReturnsFalseForExpiredToken() {
      when(jwtProperties.getExpirationMs()).thenReturn(-1000L);
      String token = jwtService.generateToken(testUser);
      assertThat(jwtService.validateToken(token)).isFalse();
    }
  }

  @Nested
  @DisplayName("isTokenValid method")
  class IsTokenValidTests {

    @Test
    void isTokenValidReturnsTrueForMatchingUser() {
      when(jwtProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
      String token = jwtService.generateToken(testUser);
      assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    void isTokenValidReturnsFalseForWrongUser() {
      when(jwtProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
      String token = jwtService.generateToken(testUser);
      User otherUser = User.builder()
          .email("other@test.com")
          .password("encodedPassword")
          .role(Role.USER)
          .build();
      assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValidReturnsFalseForExpiredToken() {
      when(jwtProperties.getExpirationMs()).thenReturn(-1000L);
      String token = jwtService.generateToken(testUser);
      assertThat(jwtService.isTokenValid(token, testUser)).isFalse();
    }
  }

  @Nested
  @DisplayName("extractEmail method")
  class ExtractEmailTests {

    @Test
    void extractEmailReturnsCorrectEmail() {
      when(jwtProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
      String token = jwtService.generateToken(testUser);
      assertThat(jwtService.extractEmail(token)).isEqualTo("test@test.com");
    }

    @Test
    void extractEmailThrowsForInvalidToken() {
      assertThatThrownBy(() -> jwtService.extractEmail("invalid.token.here"))
          .isInstanceOf(Exception.class);
    }
  }

  @Nested
  @DisplayName("isTokenExpired method")
  class IsTokenExpiredTests {

    @Test
    void isTokenExpiredReturnsFalseForFreshToken() {
      when(jwtProperties.getExpirationMs()).thenReturn(EXPIRATION_MS);
      String token = jwtService.generateToken(testUser);
      assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void isTokenExpiredReturnsTrueForExpiredToken() {
      when(jwtProperties.getExpirationMs()).thenReturn(-1000L);
      String token = jwtService.generateToken(testUser);
      assertThat(jwtService.isTokenExpired(token)).isTrue();
    }
  }
}
