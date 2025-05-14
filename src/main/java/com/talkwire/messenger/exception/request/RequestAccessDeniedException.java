package com.talkwire.messenger.exception.request;

public class RequestAccessDeniedException extends RuntimeException {
  public RequestAccessDeniedException(String message) {
    super(message);
  }
}