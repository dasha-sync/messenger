package com.talkwire.messenger.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckResponse {
  private String username;
  private String email;
  private Boolean authenticated;
}
