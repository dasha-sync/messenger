package com.talkwire.messenger.dto.user;

import lombok.*;

@Data
@AllArgsConstructor
public class UserResponse {
  private Long id;
  private String username;
  private String email;
}