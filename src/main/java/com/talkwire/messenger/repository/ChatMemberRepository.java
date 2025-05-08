package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.Chat;
import com.talkwire.messenger.model.ChatMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
  boolean existsByUserIdAndChatId(Long userId, Long chatId);

  void deleteAllByChatId(Long chatId);

  @Query("SELECT cm.chat FROM ChatMember cm WHERE cm.user.id = :userId")
  List<Chat> findChatsByUserId(@Param("userId") Long userId);

  @Query("SELECT cm1.chat FROM ChatMember cm1 "
      + "JOIN ChatMember cm2 ON cm1.chat = cm2.chat "
      + "WHERE cm1.user.id = :userId1 AND cm2.user.id = :userId2")
  List<Chat> findChatsByTwoUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
