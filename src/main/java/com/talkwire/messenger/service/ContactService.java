package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.contact.ContactResponse;
import com.talkwire.messenger.model.Contact;
import com.talkwire.messenger.model.User;
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
    // TODO: ContactNotFoundException
    Contact contact = contactRepository.findById(contactId)
        .orElseThrow(() -> new RuntimeException("Contact not found"));

    validateContactAccess(contact, currentUser.getId());
    contactRepository.delete(contact);
  }

  public ContactResponse mapToContactDto(Contact contact) {
    return new ContactResponse(
        contact.getId(),
        contact.getUser().getId(),
        contact.getUser().getUsername(),
        contact.getContact().getId(),
        contact.getContact().getUsername());
  }

  // TODO: ContactAccessDeniedException
  private void validateContactAccess(Contact contact, Long userId) {
    if (!contact.getUser().getId().equals(userId)) {
      throw new RuntimeException("Access denied: It is not your request");
    }
  }
}
