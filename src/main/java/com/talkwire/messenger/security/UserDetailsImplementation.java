package com.talkwire.messenger.security;

import com.talkwire.messenger.model.User;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adapter class that bridges the application's User entity
 * with Spring Security's UserDetails interface.
 */
@Data
@AllArgsConstructor
public class UserDetailsImplementation implements UserDetails {
  private Long id;
  private String username;
  private String email;
  private String password;

  /**
   * Builds a UserDetailsImplementation from a User entity.
   *
   * @param user the domain User
   * @return an instance of UserDetailsImplementation
   */
  public static UserDetailsImplementation build(User user) {
    return new UserDetailsImplementation(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPassword());
  }

  /**
   * Returns the authorities granted to the user.
   * No roles are defined for now.
   */
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

  /**
   * Indicates whether the user's account has expired.
   * Always true in this implementation.
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * Indicates whether the user is locked or unlocked.
   * Always true in this implementation.
   */
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * Indicates whether the user's credentials have expired.
   * Always true in this implementation.
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * Indicates whether the user is enabled.
   * Always true in this implementation.
   */
  @Override
  public boolean isEnabled() {
    return true;
  }
}
