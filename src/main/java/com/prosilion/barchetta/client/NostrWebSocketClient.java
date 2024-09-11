package com.prosilion.barchetta.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NonNull;
import nostr.event.BaseMessage;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

public class NostrWebSocketClient<T extends BaseMessage> implements Subscriber<String> {
  private final WebSocketClient webSocketClient;

  private Subscription subscription;
  @Getter
//  TODO: below needs improve/resilient
  private String relayResponse = null;

  public NostrWebSocketClient(@NonNull WebSocketClient webSocketClient) {
    this.webSocketClient = webSocketClient;
  }

  public Flux<String> send(@NonNull T baseMessage) throws JsonProcessingException {
    return send(baseMessage.encode());
  }

  public Flux<String> send(@NonNull String json) {
    Flux<String> flux = webSocketClient.sendMessageMono(json);
    flux.subscribeWith(this);
    return flux;
  }

  @Override
  public void onSubscribe(Subscription subscription) {
    System.out.println("000000000000000000");
    System.out.println("000000000000000000");
    this.subscription = subscription;
    subscription.request(1);
    System.out.println("000000000000000000");
    System.out.println("000000000000000000");
  }

  @Override
  public void onNext(String s) {
    System.out.println("11111111111111111111111111");
    System.out.println("11111111111111111111111111");
    subscription.request(1);
    relayResponse = s;
    System.out.println("11111111111111111111111111");
    System.out.println("11111111111111111111111111");
  }

  @Override
  public void onError(Throwable throwable) {
    System.out.println("222222222222222222");
    System.out.println("222222222222222222");
    System.out.println("error");
    System.out.println("222222222222222222");
    System.out.println("222222222222222222");
  }

  @Override
  public void onComplete() {
    System.out.println("333333333333333333");
    System.out.println("333333333333333333");
    System.out.println("completed");
    System.out.println("333333333333333333");
    System.out.println("333333333333333333");
  }
}