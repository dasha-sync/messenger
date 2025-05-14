package com.talkwire.messenger.exception.chat;

public class ChatAccessDeniedException extends RuntimeException {
  public ChatAccessDeniedException(String message) {
    super(message);
  }
}
