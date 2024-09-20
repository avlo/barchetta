package com.prosilion.barchetta.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;

import java.net.URI;

@Slf4j
@Component
public class NettyWebSocketClient implements WebSocketClientIF, Subscriber<String> {
  ReactorNettyWebSocketClient client;
  URI uri;

  public NettyWebSocketClient() {
    this.client = new ReactorNettyWebSocketClient();
    this.uri = URI.create("ws://localhost:5555/");
  }

  @Override
  public <T extends BaseMessage> Flux<String> send(T eventMessage) throws JsonProcessingException {
    return send(eventMessage.encode());
  }

  @Override
  public Flux<String> send(String json) {
    client.execute(uri,
        session -> session
            .send(Flux.just(session.textMessage(json)))
            .thenMany(session.receive().take(1).map(WebSocketMessage::getPayloadAsText))
            .doOnNext(s -> {
              System.out.println("************");
              System.out.println(s);
              System.out.println("************");
            }).then())
        .block();

    return null;
  }
  @Override
  public void onSubscribe(Subscription s) {

  }
  @Override
  public void onNext(String s) {

  }
  @Override
  public void onError(Throwable t) {

  }
  @Override
  public void onComplete() {

  }
}
