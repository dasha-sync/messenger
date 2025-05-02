package com.talkwire.messenger.controller;

import com.talkwire.messenger.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Handles incoming WebSocket chat messages.
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  /**
   * Processes messages sent to "/app/chat" and responds to "/topic/messages".
   *
   * @param message the incoming message from the client
   * @return JSON response with the service's reply
   */
  @MessageMapping("/chat")
  @SendTo("/topic/messages")
  public String processMessageFromClient(String message) {
    return "{\"response\" : \"" + chatService.answerMessage(message) + "\"}";
  }
}
