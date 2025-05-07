package com.talkwire.messenger.dto.user;

import lombok.Data;

@Data
public class SigninRequest {
  private String username;
  private String password;
}

