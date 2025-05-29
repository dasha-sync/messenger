package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.exception.user.UserAlreadyExistsException;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.util.JwtTokenProvider;
import jakarta.servlet.http.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.*;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
  private static final int COOKIE_MAX_AGE = 259200; // 3 days in seconds

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserService userService;

  public UserResponse signup(SignupRequest request) {
    checkIfUserExists(request.getUsername(), request.getEmail());

    User user = createUser(request);
    userRepository.save(user);
    return userService.mapToUserDto(user);
  }

  public AuthResponse signin(SigninRequest request, HttpServletResponse response) {
    try {
      Authentication auth = authenticateUser(request.getUsername(), request.getPassword());
      SecurityContextHolder.getContext().setAuthentication(auth);

      String jwt = jwtTokenProvider.generateToken(auth);
      User user = getUserByUsername(request.getUsername());

      setAuthCookies(response, jwt, user);

      return new AuthResponse(jwt, userService.mapToUserDto(user));
    } catch (BadCredentialsException | NoSuchAlgorithmException e) {
      throw new BadCredentialsException("Invalid credentials");
    }
  }

  public void signout(HttpServletResponse response) {
    clearAuthCookies(response);
  }

  public CheckResponse checkAuth(HttpServletRequest request) {
    Map<String, String> cookies = extractCookies(request);
    String jwt = cookies.get("jwt");

    return new CheckResponse(
        cookies.get("username"),
        cookies.get("email"),
        jwt != null
    );
  }

  private void checkIfUserExists(String username, String email) {
    if (userRepository.existsUserByUsername(username)) {
      throw new UserAlreadyExistsException("Username already taken");
    }

    if (userRepository.existsUserByEmail(email)) {
      throw new UserAlreadyExistsException("Email already registered");
    }
  }

  private User createUser(SignupRequest request) {
    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    return user;
  }

  private Authentication authenticateUser(String username, String password) {
    return authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, password)
    );
  }

  private User getUserByUsername(String username) {
    return userRepository.findUserByUsername(username)
        .orElseThrow(() -> new BadCredentialsException("User not found"));
  }

  private void setAuthCookies(HttpServletResponse response, String jwt, User user) {
    addCookie(response, "jwt", jwt, COOKIE_MAX_AGE);
    addCookie(response, "username", user.getUsername(), COOKIE_MAX_AGE);
    addCookie(response, "email", user.getEmail(), COOKIE_MAX_AGE);
  }

  private void clearAuthCookies(HttpServletResponse response) {
    addCookie(response, "jwt", null, 0);
    addCookie(response, "username", null, 0);
    addCookie(response, "email", null, 0);
  }

  private void addCookie(HttpServletResponse response, String name, String value, int expiration) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // Consider true for HTTPS
    cookie.setPath("/");
    cookie.setMaxAge(expiration);
    response.addCookie(cookie);
  }

  private Map<String, String> extractCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Collections.emptyMap();
    }

    return Arrays.stream(cookies)
        .collect(Collectors.toMap(Cookie::getName, Cookie::getValue, (a, b) -> b));
  }
}
