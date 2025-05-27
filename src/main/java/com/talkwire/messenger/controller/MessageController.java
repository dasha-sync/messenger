package com.talkwire.messenger.controller;

import com.talkwire.messenger.dto.common.ApiResponse;
import com.talkwire.messenger.dto.message.*;
import com.talkwire.messenger.service.MessageService;
import java.security.Principal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {
  private final MessageService messageService;
  private final SimpMessagingTemplate messagingTemplate;

  @GetMapping("/secured/chats/{chatId}/messages")
  public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
      @PathVariable Long chatId,
      Principal principal) {
    List<MessageResponse> messages = messageService.getMessages(chatId, principal);

    messagingTemplate.convertAndSendToUser(
        principal.getName(),
        "/queue/chats/" + chatId + "/messages",
        new ApiResponse<>("Messages retrieved successfully", messages));
    return ResponseEntity.ok(new ApiResponse<>("Messages retrieved successfully", messages));
  }

  @MessageMapping("/secured/chats/{chatId}/messages/create")
  @SendTo("/topic/secured/chats/{chatId}/messages")
  public MessageResponse createMessage(
      @DestinationVariable Long chatId,
      CreateMessageRequest request,
      StompHeaderAccessor headerAccessor) {
    Principal principal = headerAccessor.getUser();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null && principal != null) {
      auth = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    if (auth == null) {
      throw new AuthenticationCredentialsNotFoundException("No authentication found");
    }

    return messageService.createMessage(chatId, request, auth);
  }

  @MessageMapping("/secured/chats/{chatId}/messages/{messageId}/update")
  @SendTo("/topic/secured/chats/{chatId}/messages")
  public MessageResponse updateMessage(
      @DestinationVariable Long chatId,
      @DestinationVariable Long messageId,
      UpdateMessageRequest request,
      StompHeaderAccessor headerAccessor) {
    Principal principal = headerAccessor.getUser();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null && principal != null) {
      auth = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    if (auth == null) {
      throw new AuthenticationCredentialsNotFoundException("No authentication found");
    }
    return messageService.updateMessage(chatId, messageId, request, principal);
  }

  @MessageMapping("/secured/chats/{chatId}/messages/{messageId}/destroy")
  @SendTo("/topic/secured/chats/{chatId}/messages")
  public MessageResponse deleteMessage(
      @DestinationVariable Long chatId,
      @DestinationVariable Long messageId,
      StompHeaderAccessor headerAccessor) {
    Principal principal = headerAccessor.getUser();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null && principal != null) {
      auth = new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    if (auth == null) {
      throw new AuthenticationCredentialsNotFoundException("No authentication found");
    }

    return messageService.deleteMessage(chatId, messageId, principal);
  }
}
