package com.talkwire.messenger.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
  @Pattern(
      regexp = "^[a-z][a-z0-9_.-]*$",
      message = "Username can only contain Latin lowercase letters,"
          + "numbers, '_', '-', '.' and must begin with the letter")
  @Size(min = 6, max = 20, message = "Username must contain 6 - 20 symbols")
  private String username;

  @Email(message = "Non correct email format")
  private String email;

  @Size(min = 6, max = 20, message = "Password must contain 6 - 20 symbols")
  private String newPassword;

  @Size(min = 6, max = 20, message = "Password must contain 6 - 20 symbols")
  private String currentPassword;
}
