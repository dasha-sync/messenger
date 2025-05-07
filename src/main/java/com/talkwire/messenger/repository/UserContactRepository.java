package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.UserContact;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserContactRepository extends JpaRepository<UserContact, Long> {
  List<UserContact> findByUserId(Long userId);

  List<UserContact> findByContactId(Long contactId);

  Optional<UserContact> findByUserIdAndContactId(Long userId, Long contactId);

  boolean existsByUserIdAndContactId(Long userId, Long contactId);

  void deleteByUserIdAndContactId(Long userId, Long contactId);

  @Query("SELECT cu FROM user_contacts uc WHERE uc.user_id = :userId AND uc.contact_id = :contactId")
  Optional<UserContact> findChatUserByUserIdAndContactId(@Param("userId") Long userId, @Param("contactId") Long contactId);
}
