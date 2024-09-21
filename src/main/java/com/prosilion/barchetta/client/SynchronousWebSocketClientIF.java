package com.prosilion.barchetta.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import nostr.event.BaseMessage;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

public interface SynchronousWebSocketClientIF {
  <T extends BaseMessage> List<String> send(T eventMessage) throws IOException;
  List<String> send(String json) throws IOException;
}
