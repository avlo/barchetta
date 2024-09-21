package com.prosilion.barchetta.client;

import nostr.event.BaseMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.awaitility.Awaitility.await;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SynchronousClient extends TextWebSocketHandler implements SynchronousWebSocketClientIF {
  private final WebSocketSession clientSession;
  private List<String> returnedJson = new ArrayList<>();
  private boolean completed = false;
  private int counter = 0;

  public SynchronousClient(@Value("${nostr.relay.uri}") String relayUri) throws ExecutionException, InterruptedException {
    this.clientSession = new StandardWebSocketClient().execute(this, new WebSocketHttpHeaders(), URI.create(relayUri)).get();
  }

  @Override
  protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
    for (int i = 0; i < 10; i++) {
      System.out.print(counter);
    }
    System.out.println();
    returnedJson.add(message.getPayload());
    completed = true;
    for (int i = 0; i < 10; i++) {
      System.out.print(counter);
    }
    System.out.println();
    counter++;
  }

  @Override
  public <T extends BaseMessage> List<String> send(T eventMessage) throws IOException {
    return send(eventMessage.encode());
  }

  @Override
  public List<String> send(String json) throws IOException {
    clientSession.sendMessage(new TextMessage(json));
    await().until(() -> completed);
    List<String> toReturn = List.copyOf(returnedJson);
    returnedJson = new ArrayList<>();
    completed = false;
    counter = 0;
    return toReturn;
  }
}