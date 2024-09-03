package com.prosilion.barchetta.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import nostr.event.BaseMessage;
import reactor.core.publisher.Flux;

public class NostrWebSocketClient {
  private final WebSocketClient webSocketClient;

  public NostrWebSocketClient(@NonNull WebSocketClient webSocketClient) {
    this.webSocketClient = webSocketClient;
  }

  public Flux<String> send(@NonNull BaseMessage baseMessage) throws JsonProcessingException {
    return webSocketClient.sendMessageMono(baseMessage.encode());
  }

  public Flux<String> send(@NonNull String json) {
    return webSocketClient.sendMessageMono(json);
  }
}

