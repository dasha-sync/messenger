package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.user.SigninRequest;
import com.talkwire.messenger.dto.user.SignupRequest;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.security.JwtTokenProvider;
import java.security.NoSuchAlgorithmException;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class SecurityController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@RequestBody SignupRequest signupDto) {
    if (userRepository.existsUserByUsername(signupDto.getUsername())) {
      return ResponseEntity.badRequest().body("Username already taken");
    }

    if (userRepository.existsUserByEmail(signupDto.getEmail())) {
      return ResponseEntity.badRequest().body("Email already registered");
    }

    User user = new User();
    user.setUsername(signupDto.getUsername());
    user.setEmail(signupDto.getEmail());
    user.setPassword(passwordEncoder.encode(signupDto.getPassword()));
    userRepository.save(user);

    return ResponseEntity.ok("Signup successful");
  }

  @PostMapping("/signin")
  public ResponseEntity<?> signin(@RequestBody SigninRequest signinDto) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              signinDto.getUsername(), signinDto.getPassword())
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtTokenProvider.generateToken(authentication);

      return ResponseEntity.ok(jwt);
    } catch (BadCredentialsException | NoSuchAlgorithmException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
  }
}
