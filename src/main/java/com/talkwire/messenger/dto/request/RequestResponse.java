package com.talkwire.messenger.dto.request;

import lombok.*;

@Data
@AllArgsConstructor
public class RequestResponse {
  private Long id;
  private Long from;
  private String fromUsername;
  private Long to;
  private String toUsername;
  private String action;
}
