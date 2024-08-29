package com.prosilion.barchetta.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import nostr.event.BaseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NostrWebSocketClient {
  private final WebSocketClient webSocketClient;

  @Autowired
  public NostrWebSocketClient(@NonNull WebSocketClient webSocketClient) {
    this.webSocketClient = webSocketClient;
  }

  public void send(@NonNull BaseMessage baseMessage) throws JsonProcessingException {
//  TODO: investigate refactoring using new TextMessage & WebSocketSession
    webSocketClient.send(baseMessage.encode());
  }
}

