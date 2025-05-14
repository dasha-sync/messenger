package com.talkwire.messenger.exception.contact;

public class ContactNotFoundException extends RuntimeException {
  public ContactNotFoundException(String message) {
    super(message);
  }
}