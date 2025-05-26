package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.exception.user.*;
import com.talkwire.messenger.model.Chat;
import com.talkwire.messenger.model.Contact;
import com.talkwire.messenger.model.Request;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.repository.ChatMemberRepository;
import com.talkwire.messenger.repository.RequestRepository;
import com.talkwire.messenger.repository.ContactRepository;
import com.talkwire.messenger.util.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.security.*;
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
  private final ChatMemberRepository chatMemberRepository;
  private final RequestRepository requestRepository;
  private final ContactRepository contactRepository;

  public List<UserResponse> getUsers(FindUserRequest request) {
    List<User> users = (isBlank(request.getUsername()) && isBlank(request.getEmail()))
        ? userRepository.findAllByOrderByUsernameAsc()
        : userRepository.findByUsernameContainingIgnoreCaseAndEmailContainingIgnoreCase(
            request.getUsername(), request.getEmail());

    return users.stream()
        .map(this::mapToUserDto)
        .toList();
  }

  public UserResponse getNonCurrentUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));

    return mapToUserDto(user);
  }

  public AuthResponse updateUser(
      UpdateUserRequest request,
      Principal principal,
      HttpServletResponse response) {
    User currentUser = getCurrentUser(principal);

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
                  ? request.getCurrentPassword()
                  : request.getNewPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtTokenProvider.generateToken(authentication);

      Cookie cookie = new Cookie("jwt", jwt);
      cookie.setHttpOnly(true);
      cookie.setSecure(false);
      cookie.setPath("/");
      cookie.setMaxAge((int) (259200000));
      response.addCookie(cookie);

      return new AuthResponse(jwt, mapToUserDto(currentUser));
    } catch (BadCredentialsException | NoSuchAlgorithmException e) {
      throw new UserUpdateException("Invalid credentials");
    }
  }

  @Transactional
  public void deleteUser(
      DeleteUserRequest request,
      Principal principal,
      HttpServletResponse response) {
    User currentUser = getCurrentUser(principal);
    if (!passwordEncoder.matches(request.getPassword(), currentUser.getPassword())) {
      throw new UserDeleteException("Wrong password");
    }

    Long userId = currentUser.getId();
    userRepository.delete(currentUser);

    Cookie cookie = new Cookie("jwt", null);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);

    if (userRepository.existsById(userId)) {
      throw new UserDeleteException("Failed to delete user");
    }
  }

  public UserRelationsResponse getUserRelations(Long userId, Principal principal) {
    Long currentUserId = getCurrentUser(principal).getId();

    if (currentUserId.equals(userId)) {
      throw new UserOperationException("Relations can't be found between two equal users");
    }

    UserRelationsResponse response = new UserRelationsResponse();

    setChatRelation(response, currentUserId, userId);
    setContactRelation(response, currentUserId, userId);
    setRequestRelations(response, currentUserId, userId);

    return response;
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

  private void setChatRelation(UserRelationsResponse response, Long currentUserId, Long userId) {
    List<Chat> chats = chatMemberRepository.findChatsByTwoUsers(currentUserId, userId);
    if (!chats.isEmpty()) {
      response.setHasChat(chats.get(0).getId());
    }
  }

  private void setContactRelation(UserRelationsResponse response, Long currentUserId, Long userId) {
    contactRepository.findByUserIdAndContactId(currentUserId, userId)
        .ifPresent(contact -> response.setHasContact(contact.getId()));
  }

  private void setRequestRelations(
      UserRelationsResponse response,
      Long currentUserId,
      Long userId) {
    requestRepository.findByUserIdAndContactId(currentUserId, userId)
        .ifPresent(request -> response.setHasOutgoingRequest(request.getId()));

    requestRepository.findByUserIdAndContactId(userId, currentUserId)
        .ifPresent(request -> response.setHasIncomingRequest(request.getId()));
  }
}
