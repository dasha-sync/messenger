package com.talkwire.messenger.repository;

import com.talkwire.messenger.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing user data in the database.
 * Extends {@link JpaRepository} to provide CRUD operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user by their username.
   *
   * @param username the username of the user
   * @return an {@link Optional} containing the user if found, or empty if not found
   */
  Optional<User> findUserByUsername(String username);

  /**
   * Checks if a user exists by their username.
   *
   * @param username the username of the user
   * @return true if a user exists with the given username, otherwise false
   */
  Boolean existsUserByUsername(String username);

  /**
   * Checks if a user exists by their email.
   *
   * @param email the email of the user
   * @return true if a user exists with the given email, otherwise false
   */
  Boolean existsUserByEmail(String email);
}
