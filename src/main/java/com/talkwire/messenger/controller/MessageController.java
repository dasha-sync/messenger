package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.message.*;
import com.talkwire.messenger.service.MessageService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/secured/chats")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MessageController {
  private final MessageService messageService;
  private final SimpMessagingTemplate messagingTemplate;

  @GetMapping("{chatId}/messages")
  public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
      @PathVariable Long chatId,
      Principal principal) {
    List<MessageResponse> messages = messageService.getMessages(chatId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Messages retrieved successfully", messages));
  }

  @PostMapping("{chatId}/messages/create")
  @SendTo("/topic/chats/{chatId}/messages")
  public ResponseEntity<MessageResponse> createMessage(
      @PathVariable Long chatId,
      @Valid @RequestBody CreateMessageRequest request,
      Principal principal) {
    MessageResponse message = messageService.createMessage(chatId, request, principal);

    return ResponseEntity.ok(message);
  }

  @PatchMapping("{chatId}/messages/{messageId}/update")
  @SendTo("/topic/chats/{chatId}/messages")
  public ResponseEntity<MessageResponse> updateMessage(
      @PathVariable Long chatId,
      @PathVariable Long messageId,
      @Valid @RequestBody UpdateMessageRequest request,
      Principal principal) {
    MessageResponse message = messageService.updateMessage(chatId, messageId, request, principal);

    return ResponseEntity.ok(message);
  }

  @DeleteMapping("{chatId}/messages/{messageId}/destroy")
  @SendTo("/topic/chats/{chatId}/messages")
  public ResponseEntity<MessageResponse> deleteMessage(
      @PathVariable Long chatId,
      @PathVariable Long messageId,
      Principal principal) {
    MessageResponse message = messageService.deleteMessage(chatId, messageId, principal);

    return ResponseEntity.ok(message);
  }
}
