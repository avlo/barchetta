package com.prosilion.barchetta.client;

import lombok.NonNull;
import nostr.event.BaseMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

public class StandardWebSocketClient extends TextWebSocketHandler implements WebSocketClientIF {
  private final WebSocketSession clientSession;
  private final List<String> events = Collections.synchronizedList(new ArrayList<>());
  private final AtomicBoolean completed = new AtomicBoolean(false);

  public StandardWebSocketClient(@NonNull String relayUri, @NonNull SslBundles sslBundles) throws ExecutionException, InterruptedException {
    org.springframework.web.socket.client.standard.StandardWebSocketClient standardWebSocketClient = new org.springframework.web.socket.client.standard.StandardWebSocketClient();
    standardWebSocketClient.setSslContext(sslBundles.getBundle("server").createSslContext());
    this.clientSession = standardWebSocketClient
        .execute(
            this,
            new WebSocketHttpHeaders(),
            URI.create(relayUri))
        .get();
  }

  @Override
  protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
    events.add(message.getPayload());
//    completed = true;
    completed.setRelease(true);
  }

  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  @Override
  public List<String> send(String json) throws IOException {
    clientSession.sendMessage(new TextMessage(json));
    await().until(() -> completed.compareAndSet(true, false));
    List<String> eventList = List.copyOf(events);
//    events = Collections.synchronizedList(new ArrayList<>());
    events.clear();
//    completed = false;
    return eventList;
  }
}
