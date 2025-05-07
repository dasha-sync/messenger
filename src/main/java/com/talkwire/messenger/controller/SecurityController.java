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

@RestController
@RequestMapping("/auth")
public class SecurityController {
  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private AuthenticationManager authenticationManager;
  private JwtTokenProvider jwtTokenProvider;

  @Autowired
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Autowired
  public void setUserRepository(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Autowired
  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @Autowired
  public void setJwtCore(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @PostMapping("/signup")
  ResponseEntity<?> signup(@RequestBody SignupRequest signupDto) {
    if (userRepository.existsUserByUsername(signupDto.getUsername())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different name");
    }
    if (userRepository.existsUserByEmail(signupDto.getEmail())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different email");
    }

    String hashed = passwordEncoder.encode(signupDto.getPassword());
    User user = User.builder()
        .username(signupDto.getUsername())
        .email(signupDto.getEmail())
        .password(hashed)
        .build();
    userRepository.save(user);

    return ResponseEntity.ok("Signup successfully completed");
  }

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
