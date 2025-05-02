package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.user.SigninRequest;
import com.talkwire.messenger.dto.user.SignupRequest;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.security.JwtTokenProvider;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles authentication and user registration requests.
 */
@RestController
@RequestMapping("/auth")
public class SecurityController {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private AuthenticationManager authenticationManager;
  private JwtTokenProvider jwtTokenProvider;

  /**
   * Sets the password encoder.
   *
   * @param passwordEncoder the password encoder
   */
  @Autowired
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Sets the user repository.
   *
   * @param userRepository the user repository
   */
  @Autowired
  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Sets the authentication manager.
   *
   * @param authenticationManager the authentication manager
   */
  @Autowired
  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  /**
   * Sets the JWT token provider.
   *
   * @param jwtTokenProvider the JWT token provider
   */
  @Autowired
  public void setJwtCore(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  /**
   * Handles user signup by registering a new user.
   *
   * @param signupDto the user signup request
   * @return ResponseEntity with success or error message
   */
  @PostMapping("/signup")
  ResponseEntity<?> signup(@RequestBody SignupRequest signupDto) {
    if (userRepository.existsUserByUsername(signupDto.getUsername())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different name");
    }
    if (userRepository.existsUserByEmail(signupDto.getEmail())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different email");
    }

    String hashed = passwordEncoder.encode(signupDto.getPassword());
    User user = new User();
    user.setUsername(signupDto.getUsername());
    user.setEmail(signupDto.getEmail());
    user.setPassword(hashed);
    userRepository.save(user);
    
    return ResponseEntity.ok("Signup successfully completed");
  }

  /**
   * Handles user sign-in by authenticating the user and generating a JWT token.
   *
   * @param singinDto the user signin request
   * @return ResponseEntity with the generated JWT token or unauthorized status
   * @throws NoSuchAlgorithmException if algorithm issues occur during sign-in
   */
  @PostMapping("/signin")
  ResponseEntity<?> signin(@RequestBody SigninRequest singinDto) throws NoSuchAlgorithmException {
    Authentication authentication = null;
    try {
      authentication = authenticationManager
              .authenticate(new UsernamePasswordAuthenticationToken(
                      singinDto.getUsername(), singinDto.getPassword()));
    } catch (BadCredentialsException e) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtTokenProvider.generateToken(authentication);
    
    return ResponseEntity.ok(jwt);
  }
}
