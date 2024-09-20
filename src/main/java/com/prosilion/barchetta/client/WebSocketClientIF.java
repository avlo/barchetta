package com.prosilion.barchetta.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.event.BaseMessage;
import reactor.core.publisher.Flux;

public interface WebSocketClientIF {
  <T extends BaseMessage> Flux<String> send(T eventMessage) throws JsonProcessingException;
  Flux<String> send(String json);
}
