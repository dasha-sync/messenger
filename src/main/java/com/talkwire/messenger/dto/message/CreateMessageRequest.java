package com.talkwire.messenger.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMessageRequest {
  @NotBlank(message = "Write message")
  private String text;
}
