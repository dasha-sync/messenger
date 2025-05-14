package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserContactRepository extends JpaRepository<Contact, Long> {
}
