package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class SecurityController {
  private final AuthService authService;

  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<UserResponse>> signup(@RequestBody SignupRequest request) {
    UserResponse user = authService.signup(request);
    return ResponseEntity.ok(new ApiResponse<>("Signup successful", user));
  }

  @PostMapping("/signin")
  public ResponseEntity<ApiResponse<AuthResponse>> signin(@RequestBody SigninRequest request) {
    AuthResponse response = authService.signin(request);
    return ResponseEntity.ok(new ApiResponse<>("Signin successful", response));
  }
}
