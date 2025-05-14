package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.request.RequestResponse;
import com.talkwire.messenger.exception.UserNotFoundException;
import com.talkwire.messenger.model.Request;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.RequestRepository;
import com.talkwire.messenger.repository.UserRepository;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {
  private final UserService userService;
  private final RequestRepository requestRepository;
  private final UserRepository userRepository;

  public List<RequestResponse> getUserRequests(Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    return requestRepository.findAllByUserId(currentUser.getId())
        .stream()
        .map(this::mapToRequestDto)
        .toList();
  }

  public RequestResponse getRequestById(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    // TODO: RequestNotFoundException
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Request not found"));

    validateRequestAccess(request, currentUser.getId());
    return mapToRequestDto(request);
  }

  public RequestResponse createRequest(Long userId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    User contactUser = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("Contact user not found"));

    // TODO: RequestOperationException
    if (currentUser.getId().equals(contactUser.getId())) {
      throw new RuntimeException("You can not create request to yourself");
    }

    Request request = new Request();
    request.setUser(currentUser);
    request.setContact(contactUser);
    requestRepository.save(request);
    return mapToRequestDto(request);
  }

  public void deleteRequest(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    // TODO: RequestNotFoundException
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Request not found"));

    validateRequestAccess(request, currentUser.getId());
    requestRepository.delete(request);
  }

  // TODO: RequestAccessDeniedException
  private void validateRequestAccess(Request request, Long userId) {
    if (!request.getUser().getId().equals(userId)) {
      throw new RuntimeException("Access denied: It is not your request");
    }
  }

  private RequestResponse mapToRequestDto(Request request) {
    return new RequestResponse(
        request.getId(),
        request.getUser().getId(),
        request.getContact().getId(),
        request.getContact().getUsername());
  }
}
