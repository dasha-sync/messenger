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

    validateUserRequestAccess(request, currentUser.getId());
    return mapToRequestDto(request, "GET");
  }

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

    if (contactRepository.existsByUserIdAndContactId(currentUser.getId(), contactUser.getId())) {
      throw new RequestOperationException("Request already exists");
    }

    Request request = new Request();
    request.setUser(currentUser);
    request.setContact(contactUser);
    requestRepository.save(request);
    return mapToRequestDto(request, "CREATE");
  }

  public RequestResponse deleteUserRequest(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RequestNotFoundException("Request not found"));

    validateUserRequestAccess(request, currentUser.getId());
    requestRepository.delete(request);

    return mapToRequestDto(request, "DELETE");
  }


  public RequestResponse approveRequest(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RequestNotFoundException("Request not found"));

    validateRequestAccess(request, currentUser.getId());

    Contact contact = new Contact();
    contact.setUser(request.getUser());
    contact.setContact(request.getContact());
    contactRepository.save(contact);

    return mapToRequestDto(request, "DELETE");
  }


  public RequestResponse deleteRequest(Long requestId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new RequestNotFoundException("Request not found"));

    validateRequestAccess(request, currentUser.getId());
    requestRepository.delete(request);

    return mapToRequestDto(request, "DELETE");
  }

  private void validateUserRequestAccess(Request request, Long userId) {
    if (!request.getUser().getId().equals(userId)) {
      throw new RequestAccessDeniedException("Access denied: It is not your user request");
    }
  }

  private void validateRequestAccess(Request request, Long userId) {
    if (!request.getContact().getId().equals(userId)) {
      throw new RequestAccessDeniedException("Access denied: It is not your request");
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
