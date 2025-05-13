package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.chat.*;
import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.service.ChatService;
import java.security.Principal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/secured")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ChatController {
  private final ChatService chatService;

  @MessageMapping("/chat")
  @SendTo("/topic/messages")
  public Map<String, String> processMessageFromClient(String message) {
    return Map.of("response", chatService.answerMessage(message));
  }

  @GetMapping("/chats")
  public ResponseEntity<ApiResponse<ChatListResponse>> getUserChats(Principal principal) {
    ChatListResponse response = chatService.getUserChats(principal);
    return ResponseEntity.ok(new ApiResponse<>("Chats retrieved successfully", response));
  }

  @GetMapping("/chats/{chatId}")
  public ResponseEntity<ApiResponse<ChatResponse>> getChatById(
      @PathVariable Long chatId,
      Principal principal) {
    ChatResponse chat = chatService.getChatById(chatId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Chat retrieved successfully", chat));
  }

  // TODO: Добавить @Valid
  @PostMapping("/chats/create")
  public ResponseEntity<ApiResponse<ChatResponse>> createChat(
      @RequestBody CreateChatRequest request,
      Principal principal) {
    ChatResponse response = chatService.createChat(request, principal);
    return ResponseEntity.ok(new ApiResponse<>("Chat created successfully", response));
  }

  @DeleteMapping("/chats/{chatId}/destroy")
  public ResponseEntity<ApiResponse<Void>> deleteChat(
      @PathVariable Long chatId,
      Principal principal) {
    chatService.deleteChat(chatId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Chat deleted successfully", null));
  }
}
