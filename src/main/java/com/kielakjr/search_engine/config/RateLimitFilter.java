package com.kielakjr.search_engine.config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;

@Component
@Order(1)
@ConditionalOnBean(RedisConnectionFactory.class)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
  private final StringRedisTemplate redisTemplate;
  private static final int MAX_REQUESTS = 20;
  private static final int WINDOW_SECONDS = 60;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws IOException, ServletException {
    String ip = request.getRemoteAddr();
    String key = "rate_limit:" + ip;

    Long count = redisTemplate.opsForValue().increment(key);
    if (count == 1) {
      redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
    }

    if (count > MAX_REQUESTS) {
      response.setStatus(429);
      response.getWriter().write("Too many requests - try again later");
      return;
    }

    chain.doFilter(request, response);
  }
}
