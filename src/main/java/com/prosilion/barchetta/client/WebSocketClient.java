package com.prosilion.barchetta.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Slf4j
@Lazy
@Component
public class WebSocketClient {
  private final String relayUrl;
  private final WebSocketHandler webSocketHandler;

  @Autowired
  WebSocketClient(WebSocketHandler webSocketHandler, @Value("${nostr.relay.uri}") String relayUrl) {
    this.relayUrl = relayUrl;
    this.webSocketHandler = webSocketHandler;
    this.webSocketHandler.connect(new ReactorNettyWebSocketClient(), getURI());
  }

  void send(String json) {
    log.info("++++++++++++");
    log.info("++++++++++++");
    log.info(json);
    log.info("++++++++++++");
    log.info("++++++++++++");
    sendMessage(json);
    countdownClose();
  }

  private void sendMessage(String message) {
    Mono
        .fromRunnable(
            () -> webSocketHandler.send(message)
        )
        .thenMany(webSocketHandler.receive())
        .doOnNext(
            System.out::println
        )
        .subscribe();
  }

  private void countdownClose() {
    Mono
        .delay(Duration.ofSeconds(1))
        .publishOn(Schedulers.boundedElastic())
        .subscribe(value -> {
//          closeMethodNostr(subscriptionId);
          closeMethodForce();
        });
  }

  private void closeMethodForce() {
    webSocketHandler.disconnect();
  }

  private void closeMethodNostr(String subscriptionId) {
    sendMessage("[\"CLOSE\",\"" + subscriptionId + "\"]");
  }

  private URI getURI() {
    try {
      return new URI(relayUrl);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
