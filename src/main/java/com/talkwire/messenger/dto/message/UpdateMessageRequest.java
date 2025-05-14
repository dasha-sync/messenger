package com.talkwire.messenger.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateMessageRequest {
  @NotBlank(message = "Id can not be blank")
  private Long messageId;

  @NotBlank(message = "Message can not be blank")
  private String newContent;
}
