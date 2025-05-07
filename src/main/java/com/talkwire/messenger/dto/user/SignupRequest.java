package com.talkwire.messenger.dto.user;

import lombok.Data;

@Data
public class SignupRequest {
  private String username;
  private String email;
  private String password;
}

