package com.talkwire.messenger.controller;

import static org.springframework.http.HttpStatus.*;

import com.talkwire.messenger.dto.message.*;
import com.talkwire.messenger.model.*;
import com.talkwire.messenger.repository.*;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/secured/chats")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MessageController {

  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;

  @GetMapping("{chatId}/messages")
  public ResponseEntity<List<MessageDto>> getMessages(
      @PathVariable Long chatId, Principal principal) {
    User currentUser = getCurrentUser(principal);
    validateChatMembership(currentUser.getId(), chatId);

    List<MessageDto> messages = messageRepository.findByChatId(chatId).stream()
        .map(message -> new MessageDto(
            message.getId(),
            message.getText(),
            message.getCreatedAt(),
            message.getUser().getId(),
            message.getUser().getUsername()
        ))
        .toList();

    return ResponseEntity.ok(messages);
  }

  @PostMapping("{chatId}/messages/create")
  public ResponseEntity<String> createMessage(@PathVariable Long chatId,
                                              @RequestBody CreateMessageRequest request,
                                              Principal principal) {
    User currentUser = getCurrentUser(principal);
    validateChatMembership(currentUser.getId(), chatId);

    Chat chat = chatRepository.findById(chatId)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Chat not found"));

    Message message = new Message();
    message.setChat(chat);
    message.setUser(currentUser);
    message.setText(request.getText());
    message.setCreatedAt(LocalDateTime.now());

    messageRepository.save(message);

    return ResponseEntity.ok("Message created successfully with ID: " + message.getId());
  }

  @PatchMapping("{chatId}/messages/{messageId}/update")
  public ResponseEntity<String> updateMessage(@PathVariable Long chatId,
                                              @PathVariable Long messageId,
                                              @RequestBody UpdateMessageRequest request,
                                              Principal principal) {
    User currentUser = getCurrentUser(principal);
    validateChatMembership(currentUser.getId(), chatId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Message not found"));

    validateMessageOwnership(currentUser, message);

    message.setText(request.getNewContent());
    messageRepository.save(message);

    return ResponseEntity.ok("Message updated successfully");
  }

  @DeleteMapping("{chatId}/messages/{messageId}/destroy")
  @Transactional
  public ResponseEntity<String> deleteMessage(@PathVariable Long chatId,
                                              @PathVariable Long messageId,
                                              Principal principal) {
    User currentUser = getCurrentUser(principal);
    validateChatMembership(currentUser.getId(), chatId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Message not found"));

    validateMessageOwnership(currentUser, message);

    messageRepository.delete(message);

    return ResponseEntity.ok("Message deleted successfully");
  }

  private User getCurrentUser(Principal principal) {
    return userRepository.findUserByUsername(principal.getName())
        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Current user not found"));
  }

  private void validateChatMembership(Long userId, Long chatId) {
    if (!chatMemberRepository.existsByUserIdAndChatId(userId, chatId)) {
      throw new ResponseStatusException(FORBIDDEN, "Access denied: not a member of this chat");
    }
  }

  private void validateMessageOwnership(User user, Message message) {
    if (!message.getUser().getId().equals(user.getId())) {
      throw new ResponseStatusException(FORBIDDEN, "Access denied: cannot modify others' messages");
    }
  }
}
