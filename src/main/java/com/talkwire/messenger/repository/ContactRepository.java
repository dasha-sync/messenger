package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.Contact;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
  List<Contact> findAllByUserId(Long userId);
}
