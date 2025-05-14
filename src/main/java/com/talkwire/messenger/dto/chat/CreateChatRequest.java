package com.talkwire.messenger.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateChatRequest {
  @NotBlank(message = "Name can not be blank")
  private String username;
}
