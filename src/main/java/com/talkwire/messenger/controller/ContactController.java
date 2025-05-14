package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.contact.ContactResponse;
import com.talkwire.messenger.service.ContactService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secured/contacts")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ContactController {
  private final ContactService contactService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<ContactResponse>>> getContacts(Principal principal) {
    List<ContactResponse> response = contactService.getContacts(principal);
    return ResponseEntity.ok(new ApiResponse<>("Contacts retrieved successfully", response));
  }

  @DeleteMapping("/{contactId}/destroy")
  public ResponseEntity<ApiResponse<Void>> deleteUserRequest(
      @PathVariable Long contactId,
      Principal principal) {
    contactService.deleteContact(contactId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Contact deleted successfully", null));
  }
}
