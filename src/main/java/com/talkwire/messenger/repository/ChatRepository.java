package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.Chat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
  List<Chat> findChatByName(String name);
}
