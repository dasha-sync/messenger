package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.message.*;
import com.talkwire.messenger.service.MessageService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/secured/chats")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MessageController {
  private final MessageService messageService;

  @GetMapping("{chatId}/messages")
  public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
      @PathVariable Long chatId,
      Principal principal) {
    List<MessageResponse> messages = messageService.getMessages(chatId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Messages retrieved successfully", messages));
  }

  @PostMapping("{chatId}/messages/create")
  public ResponseEntity<ApiResponse<MessageResponse>> createMessage(
      @PathVariable Long chatId,
      @Valid @RequestBody CreateMessageRequest request,
      Principal principal) {
    MessageResponse message = messageService.createMessage(chatId, request, principal);
    return ResponseEntity.ok(new ApiResponse<>("Message created successfully", message));
  }

  @PatchMapping("{chatId}/messages/{messageId}/update")
  public ResponseEntity<ApiResponse<MessageResponse>> updateMessage(
      @PathVariable Long chatId,
      @PathVariable Long messageId,
      @Valid @RequestBody UpdateMessageRequest request,
      Principal principal) {

    MessageResponse message = messageService.updateMessage(chatId, messageId, request, principal);
    return ResponseEntity.ok(new ApiResponse<>("Message created successfully", message));
  }

  @DeleteMapping("{chatId}/messages/{messageId}/destroy")
  public ResponseEntity<ApiResponse<Void>> deleteMessage(
      @PathVariable Long chatId,
      @PathVariable Long messageId,
      Principal principal) {
    messageService.deleteMessage(chatId, messageId, principal);
    return ResponseEntity.ok(new ApiResponse<>("Message deleted successfully", null));
  }
}
