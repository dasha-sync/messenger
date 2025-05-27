package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.exception.user.UserAlreadyExistsException;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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

  public AuthResponse signin(SigninRequest request, HttpServletResponse response) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              request.getUsername(), request.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtTokenProvider.generateToken(authentication);

      User user = userRepository.findUserByUsername(request.getUsername())
          .orElseThrow(() -> new BadCredentialsException("User not found"));

      addCookie(response, "jwt", jwt, 259200);
      addCookie(response, "username", request.getUsername(), 259200);
      addCookie(response, "email", user.getEmail(), 259200);

      return new AuthResponse(jwt, userService.mapToUserDto(user));
    } catch (BadCredentialsException | NoSuchAlgorithmException e) {
      throw new BadCredentialsException("Invalid credentials");
    }
  }

  public void signout(HttpServletResponse response) {
    addCookie(response, "jwt", null, 0);
    addCookie(response, "username", null, 0);
    addCookie(response, "email", null, 0);
  }

  public CheckResponse checkAuth(HttpServletRequest request) {
    Map<String, String> cookieMap = Optional.ofNullable(request.getCookies())
        .map(Arrays::stream)
        .orElseGet(Stream::empty)
        .collect(Collectors.toMap(Cookie::getName, Cookie::getValue, (a, b) -> b));

    String jwt = cookieMap.get("jwt");
    String username = cookieMap.get("username");
    String email = cookieMap.get("email");

    boolean isAuthenticated = jwt != null;
    return new CheckResponse(username, email, isAuthenticated);
  }

  private void addCookie(HttpServletResponse response, String name, String value, int expiration) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(expiration);
    response.addCookie(cookie);
  }
}
