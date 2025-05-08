package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.chat.ChatDto;
import com.talkwire.messenger.dto.chat.CreateChatRequest;
import com.talkwire.messenger.dto.chat.DeleteChatRequest;
import com.talkwire.messenger.model.Chat;
import com.talkwire.messenger.model.ChatMember;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.ChatMemberRepository;
import com.talkwire.messenger.repository.ChatRepository;
import com.talkwire.messenger.repository.MessageRepository;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.service.ChatService;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/secured")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ChatController {
  private final ChatService chatService;
  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final UserRepository userRepository;
  private final MessageRepository messageRepository;

  @MessageMapping("/chat")
  @SendTo("/topic/messages")
  public Map<String, String> processMessageFromClient(String message) {
    return Map.of("response", chatService.answerMessage(message));
  }


  @GetMapping("/chats")
  public ResponseEntity<?> getUserChats(Principal principal) {
    User currentUser = getCurrentUser(principal);
    var chats = chatMemberRepository.findChatsByUserId(currentUser.getId())
        .stream()
        .map(chat -> new ChatDto(chat.getId(), chat.getName(currentUser)))
        .toList();

    return ResponseEntity.ok(Map.of(
        "user", currentUser.getUsername(),
        "chats", chats
    ));
  }


  @GetMapping("/chats/{chatId}")
  public ResponseEntity<?> getChatById(@PathVariable Long chatId, Principal principal) {
    User currentUser = userRepository.findUserByUsername(principal.getName())
        .orElseThrow(() -> new RuntimeException("Current user not found"));

    if (!chatMemberRepository.existsByUserIdAndChatId(currentUser.getId(), chatId)) {
      return ResponseEntity.status(403).body("Access denied");
    }

    Chat chat = chatMemberRepository.findChatsByUserId(currentUser.getId())
        .stream()
        .filter(c -> c.getId().equals(chatId))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Chat not found"));

    ChatDto chatDto = new ChatDto(chat.getId(), chat.getName(currentUser));
    return ResponseEntity.ok(chatDto);
  }

  @PostMapping("/chats/create")
  public ResponseEntity<?> createChat(@RequestBody CreateChatRequest request, Principal principal) {
    User currentUser = getCurrentUser(principal);
    User targetUser = userRepository.findUserByUsername(request.getUsername())
        .orElseThrow(() -> new RuntimeException("Target user not found"));

    if (currentUser.getId().equals(targetUser.getId())) {
      return ResponseEntity.badRequest().body("Cannot create chat with yourself");
    }

    List<Chat> existingChats = chatMemberRepository.findChatsByTwoUsers(currentUser.getId(), targetUser.getId());
    if (!existingChats.isEmpty()) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Chat already exists between these users");
    }

    Chat chat = new Chat();
    createChatMember(chat, currentUser);
    createChatMember(chat, targetUser);
    chatRepository.save(chat);

    if (chat.getChatMembers().isEmpty()) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Chat members not created");
    }

    return ResponseEntity.ok(Map.of(
        "chatId", chat.getId(),
        "message", chat.getName(currentUser) + " chat created successfully"
    ));
  }


  @DeleteMapping("/chats/{chatId}/delete")
  @Transactional
  public ResponseEntity<?> deleteChat(@PathVariable Long chatId, Principal principal) {
    try {
      User currentUser = userRepository.findUserByUsername(principal.getName())
          .orElseThrow(() -> new RuntimeException("Current user not found"));

      if (!chatMemberRepository.existsByUserIdAndChatId(currentUser.getId(), chatId)) {
        return ResponseEntity.status(403).body("Access denied: You are not a member of this chat");
      }

      chatMemberRepository.deleteAllByChatId(chatId);
      messageRepository.deleteAllByChatId(chatId);

      chatRepository.deleteById(chatId);

      if (chatRepository.existsById(chatId)) {
        return ResponseEntity.badRequest()
            .body(Map.of(
                "message", "Chat not deleted"
            ));
      }

      return ResponseEntity.ok()
          .body(Map.of(
              "message", "Chat deleted successfully"
          ));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
          .body("Error deleting chat: " + e.getMessage());
    }
  }

  private void createChatMember(Chat chat, User user) {
    ChatMember chatMember = new ChatMember();
    chatMember.setChat(chat);
    chatMember.setUser(user);
    chat.addChatMember(chatMember);
  }

  private User getCurrentUser(Principal principal) {
    return userRepository.findUserByUsername(principal.getName())
        .orElseThrow(() -> new RuntimeException("Current user not found"));
  }
}
