package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.contact.ContactResponse;
import com.talkwire.messenger.dto.request.RequestResponse;
import com.talkwire.messenger.exception.request.*;
import com.talkwire.messenger.exception.user.UserNotFoundException;
import com.talkwire.messenger.model.*;
import com.talkwire.messenger.repository.*;
import jakarta.transaction.Transactional;
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
  private final ContactRepository contactRepository;
  private final ContactService contactService;

  public List<RequestResponse> getUserRequests(Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    return requestRepository.findAllByUserId(currentUser.getId())
        .stream()
        .map(request -> mapToRequestDto(request, "GET"))
        .toList();
  }

  public List<RequestResponse> getRequests(Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    return requestRepository.findAllByContactId(currentUser.getId())
        .stream()
        .map(request -> mapToRequestDto(request, "GET"))
        .toList();
  }

  public RequestResponse getRequestById(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RequestNotFoundException("Request not found"));

    validateRequestAccess(request, currentUser.getId());
    return mapToRequestDto(request, "GET");
  }

  @Transactional
  public RequestResponse createRequest(Long userId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    User contactUser = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("Contact user not found"));

    if (currentUser.getId().equals(contactUser.getId())) {
      throw new RequestOperationException("You can not create request to yourself");
    }

    if (contactRepository.existsByUserIdAndContactId(currentUser.getId(), contactUser.getId())) {
      throw new RequestOperationException("Contact already exists");
    }

    if (requestRepository.existsByUserIdAndContactId(currentUser.getId(), contactUser.getId())) {
      throw new RequestOperationException("Request already exists");
    }

    Request request = new Request();
    request.setUser(currentUser);
    request.setContact(contactUser);
    request = requestRepository.save(request);

    if (request.getId() == null || !requestRepository.existsById(request.getId())) {
      throw new RequestOperationException("Failed to create request");
    }

    return mapToRequestDto(request, "CREATE");
  }

  @Transactional
  public RequestResponse approveRequest(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RequestNotFoundException("Request not found"));

    validateRequestAccess(request, currentUser.getId());

    if (contactRepository.existsByUserIdAndContactId(
            request.getUser().getId(), request.getContact().getId())
        || contactRepository.existsByUserIdAndContactId(
            request.getContact().getId(), request.getUser().getId())) {

      throw new RequestOperationException("Contact already exists");
    }

    // Create contact for the request initiator
    Contact contact1 = new Contact();
    contact1.setUser(request.getUser());
    contact1.setContact(request.getContact());
    contact1 = contactRepository.save(contact1);

    if (contact1.getId() == null || !contactRepository.existsById(contact1.getId())) {
      throw new RequestOperationException("Failed to create contact for request initiator");
    }

    // Create contact for the request approver
    Contact contact2 = new Contact();
    contact2.setUser(request.getContact());
    contact2.setContact(request.getUser());
    contact2 = contactRepository.save(contact2);

    if (contact2.getId() == null || !contactRepository.existsById(contact2.getId())) {
      throw new RequestOperationException("Failed to create contact for request approver");
    }

    requestRepository.delete(request);

    if (requestRepository.existsById(requestId)) {
      throw new RequestOperationException("Failed to delete request after contact creation");
    }

    return mapToRequestDto(request, "DELETE");
  }

  @Transactional
  public RequestResponse deleteRequest(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RequestNotFoundException("Request not found"));

    validateRequestAccess(request, currentUser.getId());
    requestRepository.delete(request);

    if (requestRepository.existsById(requestId)) {
      throw new RequestOperationException("Failed to delete request");
    }

    return mapToRequestDto(request, "DELETE");
  }

  private void validateRequestAccess(Request request, Long userId) {
    if (!request.getUser().getId().equals(userId) && !request.getContact().getId().equals(userId)) {
      throw new RequestAccessDeniedException("Access denied: It is not your user request");
    }
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
}
