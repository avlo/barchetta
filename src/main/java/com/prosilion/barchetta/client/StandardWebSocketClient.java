package com.prosilion.barchetta.client;

import lombok.NonNull;
import nostr.event.BaseMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.awaitility.Awaitility.await;

public class StandardWebSocketClient extends TextWebSocketHandler implements WebSocketClientIF {
  private final WebSocketSession clientSession;
  private List<String> events = new ArrayList<>();
  private boolean completed = false;

  public StandardWebSocketClient(@NonNull String relayUri) throws ExecutionException, InterruptedException {
    this.clientSession = new org.springframework.web.socket.client.standard.StandardWebSocketClient()
        .execute(
            this,
            new WebSocketHttpHeaders(),
            URI.create(relayUri)).get();
    System.out.println("33333333333");
    System.out.println("33333333333");
    System.out.println(clientSession.getId());
    System.out.println("33333333333");
    System.out.println("33333333333");
  }

  @Override
  protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
    events.add(message.getPayload());
    completed = true;
  }

  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  @Override
  public List<String> send(String json) throws IOException {
    clientSession.sendMessage(new TextMessage(json));
    await().until(() -> completed);
    List<String> eventList = List.copyOf(events);
    events = new ArrayList<>();
    completed = false;
    return eventList;
  }
}
