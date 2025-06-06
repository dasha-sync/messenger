package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.request.RequestResponse;
import com.talkwire.messenger.service.RequestService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/secured")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class RequestController {
  private final RequestService requestService;
  private final SimpMessagingTemplate messagingTemplate;

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
    return ResponseEntity.ok(new ApiResponse<>("Requests retrieved successfully", response));
  }

  @GetMapping("/requests/{requestId}")
  public ResponseEntity<ApiResponse<RequestResponse>> getRequestById(
      @PathVariable Long requestId,
      Principal principal) {
    RequestResponse request = requestService.getRequestById(requestId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Request retrieved successfully", request));
  }

  @PostMapping("/users/{userId}/requests/create")
  @SendTo("/topic/requests")
  public ResponseEntity<ApiResponse<RequestResponse>> createRequest(
      @PathVariable Long userId,
      Principal principal) {
    RequestResponse response = requestService.createRequest(userId, principal);

    return ResponseEntity.ok(new ApiResponse<>("Request created successfully", response));
  }

  @DeleteMapping("/requests/{requestId}/destroy")
  @SendTo("/topic/requests")
  public ResponseEntity<ApiResponse<RequestResponse>> deleteUserRequest(
      @PathVariable Long requestId,
      Principal principal) {
    RequestResponse response = requestService.deleteRequest(requestId, principal);

    return ResponseEntity.ok(new ApiResponse<>("Request deleted successfully", response));
  }

  @PostMapping("/requests/{requestId}/approve")
  @SendTo("/topic/requests")
  public ResponseEntity<ApiResponse<RequestResponse>> approveRequest(
      @PathVariable Long requestId,
      Principal principal) {
    RequestResponse response = requestService.approveRequest(requestId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Request approved successfully", response));
  }

  @PostMapping("/requests/{requestId}/reject")
  @SendTo("/topic/requests")
  public ResponseEntity<ApiResponse<RequestResponse>> rejectRequest(
      @PathVariable Long requestId,
      Principal principal) {
    RequestResponse response = requestService.deleteRequest(requestId, principal);

    return ResponseEntity.ok(new ApiResponse<>("Request rejected successfully", response));
  }
}
