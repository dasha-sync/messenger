package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.contact.ContactResponse;
import com.talkwire.messenger.exception.contact.*;
import com.talkwire.messenger.model.*;
import com.talkwire.messenger.repository.ContactRepository;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService {
  private final UserService userService;
  private final ContactRepository contactRepository;

  public List<ContactResponse> getContacts(Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    return contactRepository.findAllByUserId(currentUser.getId())
        .stream()
        .map(this::mapToContactDto)
        .toList();
  }

  public void deleteContact(Long contactId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Contact contact = contactRepository.findById(contactId)
        .orElseThrow(() -> new ContactNotFoundException("Contact not found"));

    validateContactAccess(contact, currentUser.getId());

    // Find and delete the reverse contact
    Contact reverseContact = contactRepository.findByUserIdAndContactId(
        contact.getContact().getId(),
        contact.getUser().getId()
    ).orElse(null);

    // Delete both contacts
    contactRepository.delete(contact);
    if (reverseContact != null) {
        contactRepository.delete(reverseContact);
    }

    // Verify deletion
    if (contactRepository.existsById(contactId) ||
        (reverseContact != null && contactRepository.existsById(reverseContact.getId()))) {
        throw new ContactOperationException("Failed to delete contact(s)");
    }
  }

  public ContactResponse mapToContactDto(Contact contact) {
    return new ContactResponse(
        contact.getId(),
        contact.getUser().getId(),
        contact.getUser().getUsername(),
        contact.getContact().getId(),
        contact.getContact().getUsername());
  }

  private void validateContactAccess(Contact contact, Long userId) {
    if (!contact.getUser().getId().equals(userId)) {
      throw new ContactAccessDeniedException("Access denied: It is not your request");
    }
  }
}
