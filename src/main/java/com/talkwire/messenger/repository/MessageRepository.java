package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.Message;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findMessageByCreatedAt(LocalDateTime createdAt);
}