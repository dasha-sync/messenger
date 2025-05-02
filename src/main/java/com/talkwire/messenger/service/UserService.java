package com.talkwire.messenger.service;

import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.security.UserDetailsImplementation;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user-specific data for authentication.
 */
@Setter
@Service
public class UserService implements UserDetailsService {

  private UserRepository userRepository;

  /**
   * Creates a service with access to the user repository.
   *
   * @param userRepository repository used for user lookup
   */
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Retrieves a user by username and wraps it in a UserDetails implementation.
   *
   * @param username the username identifying the user
   * @return UserDetails for Spring Security
   * @throws UsernameNotFoundException if no matching user is found
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository
            .findUserByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                    String.format("User '%s' not found", username)
    ));
    return UserDetailsImplementation.build(user);
  }
}

