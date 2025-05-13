package com.talkwire.messenger.service;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.security.UserDetailsImplementation;
import java.security.Principal;
import lombok.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Setter
@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository
            .findUserByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                    String.format("User '%s' not found", username)
    ));
    return UserDetailsImplementation.build(user);
  }

  // TODO: UserNotFoundException("Current user not found")
  public User getCurrentUser(Principal principal) {
    return userRepository.findUserByUsername(principal.getName())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,"Current user not found"));
  }
}

