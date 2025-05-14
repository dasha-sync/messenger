package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.contact.ContactResponse;
import com.talkwire.messenger.dto.request.RequestResponse;
import com.talkwire.messenger.exception.UserNotFoundException;
import com.talkwire.messenger.model.Contact;
import com.talkwire.messenger.model.Request;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.ContactRepository;
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
  private final ContactRepository contactRepository;
  private final ContactService contactService;

  public List<RequestResponse> getUserRequests(Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    return requestRepository.findAllByUserId(currentUser.getId())
        .stream()
        .map(this::mapToRequestDto)
        .toList();
  }

  public List<RequestResponse> getRequests(Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    return requestRepository.findAllByContactId(currentUser.getId())
        .stream()
        .map(this::mapToRequestDto)
        .toList();
  }

  public RequestResponse getRequestById(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    // TODO: RequestNotFoundException
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Request not found"));

    validateUserRequestAccess(request, currentUser.getId());
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

    // TODO: RequestOperationException
    if (contactRepository.existsByUserIdAndContactId(currentUser.getId(), contactUser.getId())) {
      throw new RuntimeException("Contact already exists");
    }

    // TODO: RequestOperationException
    if (contactRepository.existsByUserIdAndContactId(currentUser.getId(), contactUser.getId())) {
      throw new RuntimeException("Request already exists");
    }

    Request request = new Request();
    request.setUser(currentUser);
    request.setContact(contactUser);
    requestRepository.save(request);
    return mapToRequestDto(request);
  }

  public void deleteUserRequest(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    // TODO: RequestNotFoundException
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Request not found"));

    validateUserRequestAccess(request, currentUser.getId());
    requestRepository.delete(request);
  }

  public ContactResponse approveRequest(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    // TODO: RequestNotFoundException
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RuntimeException("Request not found"));

    validateRequestAccess(request, currentUser.getId());

    Contact contact = new Contact();
    contact.setUser(request.getUser());
    contact.setContact(request.getContact());
    contactRepository.save(contact);

    return contactService.mapToContactDto(contact);
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
  private void validateUserRequestAccess(Request request, Long userId) {
    if (!request.getUser().getId().equals(userId)) {
      throw new RuntimeException("Access denied: It is not your user request");
    }
  }

  // TODO: RequestAccessDeniedException
  private void validateRequestAccess(Request request, Long userId) {
    if (!request.getContact().getId().equals(userId)) {
      throw new RuntimeException("Access denied: It is not your request");
    }
  }

  private RequestResponse mapToRequestDto(Request request) {
    return new RequestResponse(
        request.getId(),
        request.getUser().getId(),
        request.getUser().getUsername(),
        request.getContact().getId(),
        request.getContact().getUsername());
  }
}
