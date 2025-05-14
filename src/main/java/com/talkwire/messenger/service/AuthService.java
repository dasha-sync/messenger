package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.exception.user.UserAlreadyExistsException;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.security.JwtTokenProvider;
import java.security.NoSuchAlgorithmException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserService userService;

  public UserResponse signup(SignupRequest signupDto) {
    if (userRepository.existsUserByUsername(signupDto.getUsername())) {
      throw new UserAlreadyExistsException("Username already taken");
    }

    if (userRepository.existsUserByEmail(signupDto.getEmail())) {
      throw new UserAlreadyExistsException("Email already registered");
    }

    User user = new User();
    user.setUsername(signupDto.getUsername());
    user.setEmail(signupDto.getEmail());
    user.setPassword(passwordEncoder.encode(signupDto.getPassword()));
    userRepository.save(user);

    return userService.mapToUserDto(user);
  }

  public AuthResponse signin(SigninRequest request) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              request.getUsername(), request.getPassword())
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtTokenProvider.generateToken(authentication);

      User user = userRepository.findUserByUsername(request.getUsername())
          .orElseThrow(() -> new BadCredentialsException("User not found"));

      return new AuthResponse(jwt, userService.mapToUserDto(user));
    } catch (BadCredentialsException | NoSuchAlgorithmException e) {
      throw new BadCredentialsException("Invalid credentials");
    }
  }
}
