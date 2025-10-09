package com.talkwire.messenger.dto.chat;

import lombok.*;

@Data
@AllArgsConstructor
public class ChatResponse {
  private Long id;
  private String name;
  private String action;
}
