package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.Message;
import com.talkwire.messenger.model.Request;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
  List<Request> findAllByUserId(Long userId);

  List<Request> findAllByContactId(Long contactId);

  boolean existsByUserIdAndContactId(Long userId, Long contactId);

  Optional<Request> findByUserIdAndContactId(Long currentUserId, Long userId);
}
