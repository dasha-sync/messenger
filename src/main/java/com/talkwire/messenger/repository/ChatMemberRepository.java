package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.ChatMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
  void deleteChatMemberByChatIdAndUserId(Long chatId, Long userId);
}
