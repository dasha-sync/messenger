package com.talkwire.messenger.util;

import java.security.Principal;
import java.util.Map;
import lombok.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
  @Override
  protected Principal determineUser(@NonNull ServerHttpRequest request,
                                    @NonNull WebSocketHandler wsHandler,
                                    Map<String, Object> attributes) {
    Authentication auth = (Authentication) attributes.get("auth");
    return auth != null ? auth : super.determineUser(request, wsHandler, attributes);
  }
}
