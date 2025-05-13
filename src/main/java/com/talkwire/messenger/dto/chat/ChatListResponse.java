package com.talkwire.messenger.dto.chat;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatListResponse {
  private String username;
  private List<ChatResponse> chats;
}
