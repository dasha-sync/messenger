package com.talkwire.messenger.controller;

import static org.springframework.http.HttpStatus.*;

import com.talkwire.messenger.dto.user.*;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/secured/users")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @GetMapping
  public ResponseEntity<List<UserDto>> getUsers(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String email
  ) {
    List<User> users;

    if ((username == null || username.isBlank()) && (email == null || email.isBlank())) {
      users = userRepository.findAllByOrderByUsernameAsc();
    } else {
      users = userRepository.findByUsernameContainingIgnoreCaseAndEmailContainingIgnoreCase(
          username != null ? username : "",
          email != null ? email : ""
      );
    }

    List<UserDto> userDtos = users.stream()
        .map(user -> new UserDto(user.getId(), user.getUsername(), user.getEmail()))
        .toList();

    return ResponseEntity.ok(userDtos);
  }

  @PatchMapping("/update")
  @Transactional
  public ResponseEntity<String> updateUser(@RequestBody UpdateUserRequest request, Principal principal) {
    User currentUser = userRepository.findUserByUsername(principal.getName())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Current user not found"));

    boolean updated = false;

    if (request.getUsername() != null && !request.getUsername().isBlank()) {
      if (userRepository.existsUserByUsername(request.getUsername())) {
        throw new ResponseStatusException(CONFLICT, "Username already taken");
      }
      currentUser.setUsername(request.getUsername());
      updated = true;
    }

    if (request.getEmail() != null && !request.getEmail().isBlank()) {
      if (userRepository.existsUserByEmail(request.getEmail())) {
        throw new ResponseStatusException(CONFLICT, "Email already registered");
      }
      currentUser.setEmail(request.getEmail());
      updated = true;
    }

    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
      updated = true;
    }

    if (updated) {
      userRepository.save(currentUser);
      return ResponseEntity.ok("User updated successfully");
    } else {
      return ResponseEntity.badRequest().body("No fields to update");
    }
  }
}
