package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.user.CreateChatRequest;
import com.talkwire.messenger.model.Chat;
import com.talkwire.messenger.model.ChatMember;
import com.talkwire.messenger.model.User;
import com.talkwire.messenger.repository.ChatMemberRepository;
import com.talkwire.messenger.repository.ChatRepository;
import com.talkwire.messenger.repository.UserRepository;
import com.talkwire.messenger.service.ChatService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
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

  @MessageMapping("/chat")
  @SendTo("/topic/messages")
  public String processMessageFromClient(String message) {
    return "{\"response\" : \"" + chatService.answerMessage(message) + "\"}";
  }

  @PostMapping("/create_chat")
  public ResponseEntity<?> createChat(@RequestBody CreateChatRequest request, Principal principal) {
    try {
      User currentUser = userRepository.findUserByUsername(principal.getName())
          .orElseThrow(() -> new RuntimeException("Current user not found"));

      User targetUser = userRepository.findUserByUsername(request.getUsername())
          .orElseThrow(() -> new RuntimeException("Target user not found"));

      if (currentUser.getId().equals(targetUser.getId())) {
        return ResponseEntity.badRequest()
            .body("Cannot create chat with yourself");
      }

      List<Chat> existingChats = chatMemberRepository.findChatsByTwoUsers(
          currentUser.getId(),
          targetUser.getId()
      );

      if (!existingChats.isEmpty()) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("Chat already exists between these users");
      }

      Chat chat = chatRepository.save(new Chat());

      // Create chat members
      createChatMember(chat, currentUser);
      createChatMember(chat, targetUser);

      return ResponseEntity.ok()
          .body(Map.of(
              "chatId", chat.getId(),
              "message", "Chat created successfully"
          ));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
          .body("Error creating chat: " + e.getMessage());
    }
  }

  private void createChatMember(Chat chat, User user) {
    ChatMember chatMember = new ChatMember();
    chatMember.setChat(chat);
    chatMember.setUser(user);
    chatMemberRepository.save(chatMember);
  }
}
