package com.talkwire.messenger.controller;

import static org.springframework.http.HttpStatus.*;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.service.UserService;
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

  @GetMapping
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

  @PatchMapping("/{userId}/update")
  public ResponseEntity<ApiResponse<AuthResponse>> updateUser(
      @PathVariable Long userId,
      @RequestBody UpdateUserRequest request,
      Principal principal) {
    AuthResponse response = userService.updateUser(userId, request, principal);
    return ResponseEntity.ok(new ApiResponse<>("User updated successfully", response));
  }

  @DeleteMapping("/{userId}/destroy")
  public ResponseEntity<ApiResponse<Void>> deleteUser(
      @PathVariable Long userId,
      @RequestBody DeleteUserRequest request,
      Principal principal) {
    userService.deleteUser(userId, request, principal);
    return ResponseEntity.ok(new ApiResponse<>("User deleted successfully", null));
  }
}
