package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class SecurityController {
  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<UserResponse>> signup(
      @Valid @RequestBody SignupRequest request
  ) {
    UserResponse user = authService.signup(request);
    return ResponseEntity.ok(new ApiResponse<>("Signup successful", user));
  }

  @PostMapping("/signin")
  public ResponseEntity<ApiResponse<AuthResponse>> signin(
      @Valid @RequestBody SigninRequest request,
      HttpServletResponse response) {
    AuthResponse authResponse = authService.signin(request, response);
    return ResponseEntity.ok(new ApiResponse<>("Signin successful", authResponse));
  }

  @PostMapping("/signout")
  public ResponseEntity<ApiResponse<Void>> signout(HttpServletResponse response) {
    authService.signout(response);
    return ResponseEntity.ok(new ApiResponse<>("Successfully loged out", null));
  }

  @GetMapping("/check")
  public ResponseEntity<CheckResponse> checkAuth(HttpServletRequest request) {
    return ResponseEntity.ok(authService.checkAuth(request));
  }
}
