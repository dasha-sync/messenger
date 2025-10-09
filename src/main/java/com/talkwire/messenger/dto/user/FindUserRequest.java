package com.talkwire.messenger.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindUserRequest {
  private String username;
  private String email;
}
