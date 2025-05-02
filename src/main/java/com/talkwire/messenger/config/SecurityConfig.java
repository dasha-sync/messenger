package com.talkwire.messenger.config;

import com.talkwire.messenger.security.TokenFilter;
import com.talkwire.messenger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;


/**
 * Configures application-level security settings.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private TokenFilter tokenFilter;
  private UserService userService;
  private final PasswordEncoder passwordEncoder;

  /**
   * Constructs the SecurityConfig with a provided password encoder.
   *
   * @param passwordEncoder the password encoder used for authentication
   */
  public SecurityConfig(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Injects the user service used for authentication.
   *
   * @param userService custom implementation of UserDetailsService
   */
  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  /**
   * Injects the token filter for processing JWTs.
   *
   * @param tokenFilter filter that validates authentication tokens
   */
  @Autowired
  public void setTokenFilter(TokenFilter tokenFilter) {
    this.tokenFilter = tokenFilter;
  }

  /**
   * Exposes the authentication manager bean.
   *
   * @param auth system-provided configuration
   * @return an authentication manager
   * @throws Exception if creation fails
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration auth)
          throws Exception {
    return auth.getAuthenticationManager();
  }

  /**
   * Configures authentication with a user service and password encoder.
   *
   * @param auth builder for authentication manager
   * @throws Exception if setup fails
   */
  @Autowired
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
  }

  /**
   * Defines security filter chain and access rules.
   *
   * @param http the HTTP security builder
   * @return the configured security filter chain
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer
                    .configurationSource(request -> new CorsConfiguration()
                            .applyPermitDefaultValues()))
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/secured/user").authenticated()
                    .anyRequest().permitAll())
            .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
