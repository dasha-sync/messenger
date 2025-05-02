package com.talkwire.messenger.controller;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles secured HTTP endpoints.
 */
@RestController
@RequestMapping("/secured")
public class MainController {

  /**
   * Returns the username of the authenticated user.
   *
   * @param principal security context holding the user identity
   * @return username if authenticated, otherwise null
   */
  @GetMapping("/user")
  public String userAccess(Principal principal) {
    if (principal == null) {
      return null;
    }
    return principal.getName();
  }
}

