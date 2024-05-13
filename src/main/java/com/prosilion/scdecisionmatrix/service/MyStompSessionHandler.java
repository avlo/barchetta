package com.prosilion.scdecisionmatrix.service;

import com.prosilion.scdecisionmatrix.service.nostr.NostrClientService;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

@Service
public class MyStompSessionHandler<T> extends StompSessionHandlerAdapter {
  WebSocketStompClient stompClient;
  Message<T> message;
  StompListener<T> listener;
  StompSession session;
  StompHeaders afterConnectedHeaders;
  StompHeaders handleFrameHeaders;

  public MyStompSessionHandler() {
    WebSocketClient webSocketClient = new StandardWebSocketClient();
    stompClient = new WebSocketStompClient(webSocketClient);
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());
  }

  @Override
  public void afterConnected(@NotNull StompSession session, @NotNull StompHeaders connectedHeaders) {
    this.session = session;
    this.afterConnectedHeaders = connectedHeaders;
    session.send("/", NostrClientService.nostrRequestString);
  }

  @Override
  public void handleFrame(@NotNull StompHeaders headers, Object payload) {
    handleFrameHeaders = headers;
    message = (Message<T>) payload;
    listener.getReturnVal(message);
  }

  public void getMessage(String url, String json, StompListener<T> listener) {
    //    session.subscribe("/topic/something", new StompFrameHandler() {
    //
    //      @Override
    //      public Type getPayloadType(StompHeaders headers) {
    //        return String.class;
    //      }
    //
    //      @Override
    //      public void handleFrame(StompHeaders headers, Object payload) {
    //        // ...
    //      }
    //
    //    });
    try {
      this.session = stompClient.connect(url, this).get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
    session.send("/", NostrClientService.nostrRequestString);
  }
}