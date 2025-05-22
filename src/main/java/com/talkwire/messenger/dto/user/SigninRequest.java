package com.talkwire.messenger.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SigninRequest {
  @Size(min = 3, max = 20, message = "Username must contain 3 - 20 symbols")
  private String username;

  @Size(min = 6, max = 20, message = "Password must contain 6 - 20 symbols")
  private String password;
}

