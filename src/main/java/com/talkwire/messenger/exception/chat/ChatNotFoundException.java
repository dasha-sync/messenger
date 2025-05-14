package com.talkwire.messenger.exception.chat;

public class ChatNotFoundException extends RuntimeException {
  public ChatNotFoundException(String message) {
    super(message);
  }
}
