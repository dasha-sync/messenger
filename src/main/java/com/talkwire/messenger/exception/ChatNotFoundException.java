package com.talkwire.messenger.exception;

public class ChatNotFoundException extends RuntimeException {
  public ChatNotFoundException(String message) {
    super(message);
  }
}
