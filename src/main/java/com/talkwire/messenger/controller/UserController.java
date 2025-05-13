package com.talkwire.messenger.controller;

import static org.springframework.http.HttpStatus.*;

import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.List;
import javax.management.remote.JMXAuthenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// TODO: facade dto
// TODO: Exceptions
@RestController
@RequestMapping("/secured/users")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserController {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;

  @GetMapping
  public ResponseEntity<List<UserResponse>> getUsers(@RequestBody FindUserRequest request) {
    List<User> users = (isBlank(request.getUsername()) && isBlank(request.getEmail()))
        ? userRepository.findAllByOrderByUsernameAsc()
        : userRepository.findByUsernameContainingIgnoreCaseAndEmailContainingIgnoreCase(
        request.getUsername(), request.getEmail()
    );

    List<UserResponse> userResponses = users.stream()
        .map(this::mapToUserDto)
        .toList();

    return ResponseEntity.ok(userResponses);
  }

  @PatchMapping("{userId}/update")
  public ResponseEntity<String> updateUser(@PathVariable Long userId,
                                           @RequestBody UpdateUserRequest request,
                                           Principal principal) {
    User currentUser = getCurrentUser(principal);
    validateUserAccess(userId, currentUser);

    if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
      throw new ResponseStatusException(FORBIDDEN, "Current password is incorrect");
    }

    if (!applyUpdates(currentUser, request)) {
      return ResponseEntity.badRequest().body("No fields to update");
    }

    userRepository.save(currentUser);

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            currentUser.getUsername(),
            request.getNewPassword().isBlank()
                ? request.getCurrentPassword() : request.getNewPassword())
    );

    try {
      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtTokenProvider.generateToken(authentication);

      return ResponseEntity.ok(jwt);
    } catch (BadCredentialsException | NoSuchAlgorithmException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }
  }

  @DeleteMapping("{userId}/destroy")
  @Transactional
  public ResponseEntity<String> deleteUser(@PathVariable Long userId, Principal principal) {
    User currentUser = getCurrentUser(principal);
    validateUserAccess(userId, currentUser);

    userRepository.delete(currentUser);

    return ResponseEntity.ok("User successfully deleted");
  }

  private UserResponse mapToUserDto(User user) {
    return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private User getCurrentUser(Principal principal) {
    return userRepository.findUserByUsername(principal.getName())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Current user not found"));
  }

  private void validateUserAccess(Long userId, User currentUser) {
    if (!currentUser.getId().equals(userId)) {
      throw new ResponseStatusException(FORBIDDEN, "You can only update your own account");
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
