package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.request.RequestResponse;
import com.talkwire.messenger.exception.request.*;
import com.talkwire.messenger.exception.user.UserNotFoundException;
import com.talkwire.messenger.model.*;
import com.talkwire.messenger.repository.*;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {
  private final UserService userService;
  private final RequestRepository requestRepository;
  private final UserRepository userRepository;
  private final ContactRepository contactRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final ContactService contactService;

  public List<RequestResponse> getUserRequests(Principal principal) {
    Long userId = getCurrentUserId(principal);
    return mapRequestsToDto(requestRepository.findAllByUserId(userId));
  }

  public List<RequestResponse> getRequests(Principal principal) {
    Long userId = getCurrentUserId(principal);
    return mapRequestsToDto(requestRepository.findAllByContactId(userId));
  }

  public RequestResponse getRequestById(Long requestId, Principal principal) {
    Request request = getRequestOrThrow(requestId);
    validateRequestAccess(request, getCurrentUserId(principal));
    return mapToRequestDto(request, "GET");
  }

  @Transactional
  public RequestResponse createRequest(Long userId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    User contactUser = getUserOrThrow(userId);
    validateCreateRequest(currentUser, contactUser);

    Request request = new Request();
    request.setUser(currentUser);
    request.setContact(contactUser);

    request = requestRepository.save(request);
    ensureSaved(request.getId(), requestRepository::existsById, "Failed to create request");

    sendWebSocketSentRequest(request, currentUser, "CREATE");
    sendWebSocketReceivedRequest(request, contactUser, "CREATE");

    return mapToRequestDto(request, "CREATE");
  }

  @Transactional
  public RequestResponse approveRequest(Long requestId, Principal principal) {
    Request request = getRequestOrThrow(requestId);
    validateRequestAccess(request, getCurrentUserId(principal));

    if (contactsExistBetween(request.getUser(), request.getContact())) {
      throw new RequestOperationException("Contact already exists");
    }

    saveContactOrThrow(request.getUser(), request.getContact(), "request initiator");
    saveContactOrThrow(request.getContact(), request.getUser(), "request approver");

    sendWebSocketSentRequest(request, request.getUser(), "DELETE");
    sendWebSocketReceivedRequest(request, request.getContact(), "DELETE");

    requestRepository.delete(request);
    ensureDeleted(
        requestId,
        requestRepository::existsById,
        "Failed to delete request after contact creation"
    );

    return mapToRequestDto(request, "DELETE");
  }

  @Transactional
  public RequestResponse deleteRequest(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Request request = getRequestOrThrow(requestId);
    validateRequestAccess(request, currentUser.getId());
    requestRepository.delete(request);

    ensureDeleted(requestId, requestRepository::existsById, "Failed to delete request");

    sendWebSocketSentRequest(request, request.getUser(), "DELETE");
    sendWebSocketReceivedRequest(request, request.getContact(), "DELETE");

    return mapToRequestDto(request, "DELETE");
  }

  private Long getCurrentUserId(Principal principal) {
    return userService.getCurrentUser(principal).getId();
  }

  private User getUserOrThrow(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("Contact user not found"));
  }

  private Request getRequestOrThrow(Long requestId) {
    return requestRepository.findById(requestId)
        .orElseThrow(() -> new RequestNotFoundException("Request not found"));
  }

  private void validateCreateRequest(User currentUser, User contactUser) {
    if (currentUser.getId().equals(contactUser.getId())) {
      throw new RequestOperationException("You can not create request to yourself");
    }

    if (contactRepository.existsByUserIdAndContactId(currentUser.getId(), contactUser.getId())) {
      throw new RequestOperationException("Contact already exists");
    }

    if (requestRepository.existsByUserIdAndContactId(currentUser.getId(), contactUser.getId())
        || requestRepository.existsByUserIdAndContactId(contactUser.getId(), currentUser.getId())) {
      throw new RequestOperationException("Request already exists");
    }
  }

  private void validateRequestAccess(Request request, Long userId) {
    if (!request.getUser().getId().equals(userId) && !request.getContact().getId().equals(userId)) {
      throw new RequestAccessDeniedException("Access denied: It is not your user request");
    }
  }

  private boolean contactsExistBetween(User user1, User user2) {
    return contactRepository.existsByUserIdAndContactId(user1.getId(), user2.getId())
        || contactRepository.existsByUserIdAndContactId(user2.getId(), user1.getId());
  }

  private void saveContactOrThrow(User user, User contact, String role) {
    Contact contactEntity = new Contact();
    contactEntity.setUser(user);
    contactEntity.setContact(contact);
    contactEntity = contactRepository.save(contactEntity);
    ensureSaved(
        contactEntity.getId(),
        contactRepository::existsById, "Failed to create contact for " + role
    );
    sendWebSocketContact(contactEntity, user);
  }

  private void ensureSaved(
      Long id,
      java.util.function.Predicate<Long> existsCheck,
      String message) {
    if (id == null || !existsCheck.test(id)) {
      throw new RequestOperationException(message);
    }
  }

  private void ensureDeleted(
      Long id,
      java.util.function.Predicate<Long> existsCheck,
      String message) {
    if (existsCheck.test(id)) {
      throw new RequestOperationException(message);
    }
  }

  private List<RequestResponse> mapRequestsToDto(List<Request> requests) {
    return requests.stream()
        .map(request -> mapToRequestDto(request, "GET"))
        .toList();
  }

  private RequestResponse mapToRequestDto(Request request, String action) {
    return new RequestResponse(
        request.getId(),
        request.getUser().getId(),
        request.getUser().getUsername(),
        request.getContact().getId(),
        request.getContact().getUsername(),
        action
    );
  }

  private void sendWebSocketSentRequest(Request request, User user, String action) {
    messagingTemplate.convertAndSend(
        "/topic/sent_requests/" + user.getUsername(),
        mapToRequestDto(request, action)
    );
  }

  private void sendWebSocketReceivedRequest(Request request, User user, String action) {
    messagingTemplate.convertAndSend(
        "/topic/received_requests/" + user.getUsername(),
        mapToRequestDto(request, action)
    );
  }

  private void sendWebSocketContact(Contact contact, User user) {
    messagingTemplate.convertAndSend(
        "/topic/contacts/" + user.getUsername(),
        contactService.mapToContactDto(contact, "CREATE"));
  }
}
