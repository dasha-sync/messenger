package com.talkwire.messenger.controller;

import com.talkwire.messenger.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
  private final ChatService chatService;

  @MessageMapping("/chat")
  @SendTo("/topic/messages")
  public String processMessageFromClient(String message) {
    return "{\"response\" : \"" + chatService.answerMessage(message) + "\"}";
  }
}
