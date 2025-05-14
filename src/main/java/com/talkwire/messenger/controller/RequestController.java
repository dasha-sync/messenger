package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.request.RequestResponse;
import com.talkwire.messenger.service.RequestService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secured")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class RequestController {
  private final RequestService requestService;

  @GetMapping("/requests")
  public ResponseEntity<ApiResponse<List<RequestResponse>>> getUserRequests(Principal principal) {
    List<RequestResponse> response = requestService.getUserRequests(principal);
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
  public ResponseEntity<ApiResponse<Void>> deleteRequest(
      @PathVariable Long requestId,
      Principal principal) {
    requestService.deleteRequest(requestId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Request deleted successfully", null));
  }
}
