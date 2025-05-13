package com.talkwire.messenger.service;

import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.security.UserDetailsImplementation;
import lombok.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Setter
@Service
@AllArgsConstructor
public class UserSecureService implements UserDetailsService {
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
}
