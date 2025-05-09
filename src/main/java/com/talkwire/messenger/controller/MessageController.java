package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.message.CreateMessageRequest;
import com.talkwire.messenger.dto.message.UpdateMessageRequest;
import com.talkwire.messenger.model.Chat;
import com.talkwire.messenger.model.Message;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.ChatMemberRepository;
import com.talkwire.messenger.repository.ChatRepository;
import com.talkwire.messenger.repository.MessageRepository;
import com.talkwire.messenger.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secured/chats/{chatId}/messages")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MessageController {
  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final MessageRepository messageRepository;
  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<?> getMessages(@PathVariable Long chatId, Principal principal) {
    User currentUser = getCurrentUser(principal);
    if (!chatMemberRepository.existsByUserIdAndChatId(currentUser.getId(), chatId)) {
      return ResponseEntity.status(403).body("Access denied");
    }

    List<Message> messages = messageRepository.findByChatId(chatId);
    return ResponseEntity.ok(messages);
  }

  @PostMapping("/create")
  @Transactional
  public ResponseEntity<?> createMessage(@PathVariable Long chatId, @RequestBody CreateMessageRequest request, Principal principal) {
    User currentUser = getCurrentUser(principal);
    if (!chatMemberRepository.existsByUserIdAndChatId(currentUser.getId(), chatId)) {
      return ResponseEntity.status(403).body("Access denied");
    }

    Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
    Message message = new Message();
    message.setChat(chat);
    message.setUser(currentUser);
    message.setText(request.getText());
    message.setCreatedAt(LocalDateTime.now());

    messageRepository.save(message);

    return ResponseEntity.ok(Map.of(
        "messageId", message.getId(),
        "message", "Message created successfully"
    ));
  }

  @PutMapping("/update")
  @Transactional
  public ResponseEntity<?> updateMessage(@PathVariable Long chatId, @RequestBody UpdateMessageRequest request, Principal principal) {
    User currentUser = getCurrentUser(principal);
    if (!chatMemberRepository.existsByUserIdAndChatId(currentUser.getId(), chatId)) {
      return ResponseEntity.status(403).body("Access denied");
    }

    Message message = messageRepository.findById(request.getMessageId())
        .orElseThrow(() -> new RuntimeException("Message not found"));

    if (!message.getUser().getId().equals(currentUser.getId())) {
      return ResponseEntity.status(403).body("Cannot edit others' messages");
    }

    message.setText(request.getNewContent());
    messageRepository.save(message);

    return ResponseEntity.ok(Map.of(
        "message", "Message updated successfully"
    ));
  }

  @DeleteMapping("/{messageId}/destroy")
  @Transactional
  public ResponseEntity<?> deleteMessage(@PathVariable Long chatId, @PathVariable Long messageId, Principal principal) {
    User currentUser = getCurrentUser(principal);
    if (!chatMemberRepository.existsByUserIdAndChatId(currentUser.getId(), chatId)) {
      return ResponseEntity.status(403).body("Access denied");
    }

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new RuntimeException("Message not found"));

    if (!message.getUser().getId().equals(currentUser.getId())) {
      return ResponseEntity.status(403).body("Cannot delete others' messages");
    }

    messageRepository.delete(message);

    return ResponseEntity.ok(Map.of(
        "message", "Message deleted successfully"
    ));
  }

  private User getCurrentUser(Principal principal) {
    return userRepository.findUserByUsername(principal.getName())
        .orElseThrow(() -> new RuntimeException("Current user not found"));
  }
}
