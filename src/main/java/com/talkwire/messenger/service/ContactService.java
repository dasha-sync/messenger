package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.contact.ContactResponse;
import com.talkwire.messenger.exception.contact.*;
import com.talkwire.messenger.model.*;
import com.talkwire.messenger.repository.ContactRepository;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService {
  private final UserService userService;
  private final ContactRepository contactRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public List<ContactResponse> getContacts(Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    return contactRepository.findAllByUserId(currentUser.getId())
        .stream()
        .map(contact -> mapToContactDto(contact, "GET"))
        .toList();
  }

  public void deleteContact(Long contactId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Contact contact = getContactById(contactId);

    validateContactAccess(contact, currentUser.getId());

    Contact reverseContact = findReverseContact(contact);

    deleteIfExists(contact);
    deleteIfExists(reverseContact);

    ensureContactsDeleted(contact, reverseContact);

    sendThrowWebSocketIfExist(contact);
    sendThrowWebSocketIfExist(reverseContact);
  }

  private Contact getContactById(Long contactId) {
    return contactRepository.findById(contactId)
        .orElseThrow(() -> new ContactNotFoundException("Contact not found"));
  }

  private Contact findReverseContact(Contact contact) {
    return contactRepository.findByUserIdAndContactId(
        contact.getContact().getId(),
        contact.getUser().getId()
    ).orElse(null);
  }

  private void deleteIfExists(Contact contact) {
    if (contact != null) {
      contactRepository.delete(contact);
    }
  }

  private void ensureContactsDeleted(Contact contact, Contact reverseContact) {
    boolean contactStillExists = contactRepository.existsById(contact.getId());
    boolean reverseStillExists = reverseContact != null
        && contactRepository.existsById(reverseContact.getId());

    if (contactStillExists || reverseStillExists) {
      throw new ContactOperationException("Failed to delete contact(s)");
    }
  }

  public ContactResponse mapToContactDto(Contact contact, String action) {
    return new ContactResponse(
        contact.getId(),
        contact.getUser().getId(),
        contact.getUser().getUsername(),
        contact.getContact().getId(),
        contact.getContact().getUsername(),
        action);
  }

  private void validateContactAccess(Contact contact, Long userId) {
    if (!contact.getUser().getId().equals(userId)) {
      throw new ContactAccessDeniedException("Access denied: It is not your request");
    }
  }

  private void sendThrowWebSocketIfExist(Contact contact) {
    if (contact != null) {
      messagingTemplate.convertAndSend(
          "/topic/contacts/" + contact.getUser().getUsername(),
          mapToContactDto(contact, "DELETE"));
    }
  }
}
