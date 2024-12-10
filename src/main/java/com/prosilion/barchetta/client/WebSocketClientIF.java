package com.prosilion.barchetta.client;

import nostr.event.BaseMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

public interface WebSocketClientIF {
  <T extends BaseMessage> void send(T eventMessage) throws IOException;
  WebSocketSession getClientSession();
  List<String> getEvents();
  void send(String json) throws IOException;
}
