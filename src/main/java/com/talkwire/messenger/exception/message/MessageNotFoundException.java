package com.talkwire.messenger.exception.message;

public class MessageNotFoundException extends RuntimeException {
  public MessageNotFoundException(String message) {
    super(message);
  }
}
