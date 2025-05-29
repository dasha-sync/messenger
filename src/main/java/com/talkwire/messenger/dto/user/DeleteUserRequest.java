package com.talkwire.messenger.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeleteUserRequest {
  @Size(min = 6, max = 20, message = "Password must contain 6 - 20 symbols")
  private String password;
}