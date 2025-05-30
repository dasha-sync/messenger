package com.talkwire.messenger.util;

import com.talkwire.messenger.model.User;
import java.util.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@AllArgsConstructor
public class UserDetailsImplementation implements UserDetails {
  private Long id;
  private String username;
  private String email;
  private String password;

  public static UserDetailsImplementation build(User user) {
    return new UserDetailsImplementation(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPassword());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
