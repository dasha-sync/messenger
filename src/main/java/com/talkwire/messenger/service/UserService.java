package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.exception.user.*;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.*;
import com.talkwire.messenger.util.*;
import jakarta.servlet.http.*;
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
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final ChatMemberRepository chatMemberRepository;
  private final RequestRepository requestRepository;
  private final ContactRepository contactRepository;

  public List<UserResponse> getUsers(FindUserRequest request, Principal principal) {
    User currentUser = getCurrentUser(principal);
    List<User> users = isBlank(request.getUsername()) && isBlank(request.getEmail())
        ? userRepository.findAllByOrderByUsernameAsc()
        : userRepository.findByUsernameContainingIgnoreCaseAndEmailContainingIgnoreCase(
            request.getUsername(), request.getEmail());

    return users.stream()
        .filter(user -> !user.getId().equals(currentUser.getId()))
        .map(this::mapToUserDto)
        .toList();
  }

  public UserResponse getNonCurrentUser(Long userId) {
    return userRepository.findById(userId)
        .map(this::mapToUserDto)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
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

    String rawPassword = request.getNewPassword().isBlank()
        ? request.getCurrentPassword()
        : request.getNewPassword();
    String jwt = authenticateAndGenerateToken(currentUser.getUsername(), rawPassword);

    setAuthCookies(response, jwt, request.getUsername(), request.getEmail());

    return new AuthResponse(jwt, mapToUserDto(currentUser));
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

    if (userRepository.existsById(userId)) {
      throw new UserDeleteException("Failed to delete user");
    }

    clearAuthCookies(response);
  }

  public UserRelationsResponse getUserRelations(Long userId, Principal principal) {
    Long currentUserId = getCurrentUser(principal).getId();

    if (currentUserId.equals(userId)) {
      throw new UserOperationException("Relations can't be found between two equal users");
    }

    UserRelationsResponse response = new UserRelationsResponse();
    setUserRelations(response, currentUserId, userId);

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

  private String authenticateAndGenerateToken(String username, String password) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, password));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      return jwtTokenProvider.generateToken(authentication);
    } catch (BadCredentialsException | NoSuchAlgorithmException e) {
      throw new UserUpdateException("Invalid credentials");
    }
  }

  private void setAuthCookies(
      HttpServletResponse response,
      String jwt,
      String username,
      String email) {
    addCookie(response, "jwt", jwt, 259200000);
    addCookie(response, "username", username, 259200000);
    addCookie(response, "email", email, 259200000);
  }

  private void clearAuthCookies(HttpServletResponse response) {
    addCookie(response, "jwt", null, 0);
    addCookie(response, "username", null, 0);
    addCookie(response, "email", null, 0);
  }

  private void addCookie(HttpServletResponse response, String name, String value, int expiration) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(expiration);
    response.addCookie(cookie);
  }

  private void setUserRelations(UserRelationsResponse response, Long currentUserId, Long userId) {
    chatMemberRepository.findChatsByTwoUsers(currentUserId, userId).stream()
        .findFirst()
        .ifPresent(chat -> response.setHasChat(chat.getId()));

    contactRepository.findByUserIdAndContactId(currentUserId, userId)
        .ifPresent(contact -> response.setHasContact(contact.getId()));

    requestRepository.findByUserIdAndContactId(currentUserId, userId)
        .ifPresent(request -> response.setHasOutgoingRequest(request.getId()));

    requestRepository.findByUserIdAndContactId(userId, currentUserId)
        .ifPresent(request -> response.setHasIncomingRequest(request.getId()));
  }
}
