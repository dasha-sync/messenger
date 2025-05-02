package com.talkwire.messenger.dto.user;

import lombok.Data;

/**
 * DTO class for handling user sign-in requests.
 */
@Data
public class SigninRequest {

  /**
   * The username of the user attempting to sign in.
   */
  private String username;

  /**
   * The password of the user attempting to sign in.
   */
  private String password;
}

