package com.talkwire.messenger.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class GlobalException extends RuntimeException {
  private HttpStatus status;

  public GlobalException(String message, String status) {
    super(message);
    setStatus(HttpStatus.valueOf(status));
  }
}
