package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.ChatUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
  List<ChatUser> findByUserId(Long userId);

  List<ChatUser> findByChatId(Long chatId);

  Optional<ChatUser> findByUserIdAndChatId(Long userId, Long chatId);

  boolean existsByUserIdAndChatId(Long userId, Long chatId);

  void deleteByUserIdAndChatId(Long userId, Long chatId);

  @Query("SELECT cu FROM ChatUser cu WHERE cu.chat.id = :chatId AND cu.user.id = :userId")
  Optional<ChatUser> findChatUserByChatIdAndUserId(@Param("chatId") Long chatId, @Param("userId") Long userId);
}
