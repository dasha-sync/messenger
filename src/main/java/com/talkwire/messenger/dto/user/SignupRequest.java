package com.talkwire.messenger.dto.user;

import lombok.Data;

/**
 * DTO class for handling user sign-up requests.
 */
@Data
public class SignupRequest {

  /**
   * The username of the user attempting to sign up.
   */
  private String username;

  /**
   * The email address of the user attempting to sign up.
   */
  private String email;

  /**
   * The password of the user attempting to sign up.
   */
  private String password;
}

