package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.contact.ContactResponse;
import com.talkwire.messenger.dto.request.RequestResponse;
import com.talkwire.messenger.service.RequestService;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secured")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class RequestController {
  private final RequestService requestService;

  // requests которые юзер сам отправил
  @GetMapping("/user_requests")
  public ResponseEntity<ApiResponse<List<RequestResponse>>> getUserRequests(Principal principal) {
    List<RequestResponse> response = requestService.getUserRequests(principal);
    return ResponseEntity.ok(new ApiResponse<>("User request retrieved successfully", response));
  }

  // requests которые юзер получил
  @GetMapping("/requests")
  public ResponseEntity<ApiResponse<List<RequestResponse>>> getRequests(Principal principal) {
    List<RequestResponse> response = requestService.getRequests(principal);
    return ResponseEntity.ok(new ApiResponse<>("Request retrieved successfully", response));
  }

  @GetMapping("/requests/{requestId}")
  public ResponseEntity<ApiResponse<RequestResponse>> getRequestById(
      @PathVariable Long requestId,
      Principal principal) {
    RequestResponse request = requestService.getRequestById(requestId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Request retrieved successfully", request));
  }

  @PostMapping("/users/{userId}/requests/create")
  public ResponseEntity<ApiResponse<RequestResponse>> createRequest(
      @PathVariable Long userId,
      Principal principal) {
    RequestResponse response = requestService.createRequest(userId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Request created successfully", response));
  }

  @DeleteMapping("/requests/{requestId}/destroy")
  public ResponseEntity<ApiResponse<Void>> deleteUserRequest(
      @PathVariable Long requestId,
      Principal principal) {
    requestService.deleteUserRequest(requestId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Request deleted successfully", null));
  }

  @PostMapping("/requests/{requestId}/approve")
  @Transactional
  public ResponseEntity<ApiResponse<ContactResponse>> approveRequest(
      @PathVariable Long requestId,
      Principal principal) {
    ContactResponse response = requestService.approveRequest(requestId, principal);
    requestService.deleteRequest(requestId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Request approved", response));
  }

  @PostMapping("/requests/{requestId}/reject")
  @Transactional
  public ResponseEntity<ApiResponse<Void>> rejectRequest(
      @PathVariable Long requestId,
      Principal principal) {
    requestService.deleteRequest(requestId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Request rejected", null));
  }
}
