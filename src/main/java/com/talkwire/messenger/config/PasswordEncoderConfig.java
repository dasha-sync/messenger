package com.talkwire.messenger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for password encoding.
 */
@Configuration
public class PasswordEncoderConfig {

  /**
   * Provides a BCrypt-based password encoder bean.
   * Used for secure password hashing.
   *
   * @return a PasswordEncoder instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

