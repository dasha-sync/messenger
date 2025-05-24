package com.talkwire.messenger.dto.user;

import lombok.*;

@Data
@NoArgsConstructor
public class UserRelationsResponse {
  private Long hasChat = null;
  private Long hasContact = null;
  private Long hasOutgoingRequest = null;
  private Long hasIncomingRequest = null;
}
