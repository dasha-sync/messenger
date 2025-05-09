package com.talkwire.messenger.dto.message;

import lombok.Data;

@Data
public class UpdateMessageRequest {
  private Long messageId;
  private String newContent;
}
