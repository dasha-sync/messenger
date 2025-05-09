package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
  void deleteAllByChatId(Long chatId);
}
