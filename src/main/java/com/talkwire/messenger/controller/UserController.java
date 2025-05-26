package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/secured/users")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping
  public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers(
      @RequestBody FindUserRequest request) {
    List<UserResponse> users = userService.getUsers(request);
    return ResponseEntity.ok(new ApiResponse<>("Users retrieved successfully", users));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<UserResponse>> getNonCurrentUser(@PathVariable Long userId) {
    UserResponse user = userService.getNonCurrentUser(userId);
    return ResponseEntity.ok(new ApiResponse<>("User retrieved successfully", user));
  }

  @PatchMapping("/update")
  public ResponseEntity<ApiResponse<AuthResponse>> updateUser(
      @Valid @RequestBody UpdateUserRequest request,
      Principal principal, HttpServletResponse response) {
    AuthResponse responseAuth = userService.updateUser(request, principal, response);
    return ResponseEntity.ok(new ApiResponse<>("User updated successfully", responseAuth));
  }

  @DeleteMapping("/destroy")
  public ResponseEntity<ApiResponse<Void>> deleteUser(
      @Valid @RequestBody DeleteUserRequest request,
      Principal principal,
      HttpServletResponse response) {
    userService.deleteUser(request, principal, response);
    return ResponseEntity.ok(new ApiResponse<>("User deleted successfully", null));
  }

  @GetMapping("/{userId}/relations")
  public ResponseEntity<ApiResponse<UserRelationsResponse>> getUserRelations(
      @PathVariable Long userId,
      Principal principal) {
    UserRelationsResponse response = userService.getUserRelations(userId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Relations checked successfully", response));
  }
}
