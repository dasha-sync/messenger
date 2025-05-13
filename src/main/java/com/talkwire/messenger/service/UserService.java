package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.exception.*;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.security.*;
import jakarta.transaction.Transactional;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.List;
import lombok.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;

  public List<UserResponse> getUsers(FindUserRequest request) {
    List<User> users = (isBlank(request.getUsername()) && isBlank(request.getEmail()))
        ? userRepository.findAllByOrderByUsernameAsc()
        : userRepository.findByUsernameContainingIgnoreCaseAndEmailContainingIgnoreCase(
        request.getUsername(), request.getEmail()
    );

    return users.stream()
        .map(this::mapToUserDto)
        .toList();
  }

  public AuthResponse updateUser(Long userId, UpdateUserRequest request, Principal principal) {
    User currentUser = getCurrentUser(principal);
    validateUserAccess(userId, currentUser);

    if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
      throw new UserUpdateException("Current password is incorrect");
    }

    if (!applyUpdates(currentUser, request)) {
      throw new UserUpdateException("No fields to update");
    }

    userRepository.save(currentUser);

    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              currentUser.getUsername(),
              request.getNewPassword().isBlank()
                  ? request.getCurrentPassword() : request.getNewPassword())
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtTokenProvider.generateToken(authentication);

      return new AuthResponse(jwt, mapToUserDto(currentUser));
    } catch (BadCredentialsException | NoSuchAlgorithmException e) {
      throw new UserUpdateException("Invalid credentials");
    }
  }

  @Transactional
  public void deleteUser(Long userId, DeleteUserRequest request, Principal principal) {
    User currentUser = getCurrentUser(principal);
    validateUserAccess(userId, currentUser);

    if (!passwordEncoder.matches(request.getPassword(), currentUser.getPassword())) {
      throw new UserDeleteException("Wrong password");
    }

    userRepository.delete(currentUser);
  }

  public User getCurrentUser(Principal principal) {
    return userRepository.findUserByUsername(principal.getName())
        .orElseThrow(() -> new UserNotFoundException("Current user not found"));
  }

  public UserResponse mapToUserDto(User user) {
    return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private void validateUserAccess(Long userId, User currentUser) {
    if (!currentUser.getId().equals(userId)) {
      throw new UserUpdateException("You can only update your own account");
    }
  }

  private boolean applyUpdates(User user, UpdateUserRequest request) {
    boolean updated = false;

    if (hasChanged(request.getUsername(), user.getUsername())) {
      user.setUsername(request.getUsername());
      updated = true;
    }

    if (hasChanged(request.getEmail(), user.getEmail())) {
      user.setEmail(request.getEmail());
      updated = true;
    }

    if (!isBlank(request.getNewPassword())
        && !passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
      user.setPassword(passwordEncoder.encode(request.getNewPassword()));
      updated = true;
    }

    return updated;
  }

  private boolean hasChanged(String newValue, String currentValue) {
    return !isBlank(newValue) && !newValue.equals(currentValue);
  }
}
