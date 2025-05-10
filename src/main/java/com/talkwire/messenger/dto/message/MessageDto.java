package com.talkwire.messenger.dto.message;

import java.time.LocalDateTime;
import lombok.*;

@Data
@AllArgsConstructor
public class MessageDto {
  private Long id;
  private String text;
  private LocalDateTime createdAt;
  private Long userId;
  private String username;
}
