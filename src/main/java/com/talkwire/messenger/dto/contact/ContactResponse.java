package com.talkwire.messenger.dto.contact;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContactResponse {
  private Long id;
  private Long from;
  private String fromUsername;
  private Long to;
  private String toUsername;
  private String action;
}
