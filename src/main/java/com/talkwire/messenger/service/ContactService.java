package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.contact.ContactResponse;
import com.talkwire.messenger.model.Contact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService {
  public ContactResponse mapToContactDto(Contact contact) {
    return new ContactResponse(
        contact.getId(),
        contact.getUser().getId(),
        contact.getUser().getUsername(),
        contact.getContact().getId(),
        contact.getContact().getUsername());
  }
}
