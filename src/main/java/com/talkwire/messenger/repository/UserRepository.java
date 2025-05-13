package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findUserByUsername(String username);

  Boolean existsUserByUsername(String username);

  Boolean existsUserByEmail(String email);

  List<User> findAllByOrderByUsernameAsc();

  List<User> findByUsernameContainingIgnoreCaseAndEmailContainingIgnoreCase(String username,
                                                                            String email);

}
