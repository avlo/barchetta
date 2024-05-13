package com.prosilion.scdecisionmatrix.service.nostr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@Service
public class NostrClientServiceImpl<T> implements NostrClientService<T> {
  Logger LOGGER = LoggerFactory.getLogger(NostrClientServiceImpl.class);

  @Override
  public Void getClassifiedListingEvent(String event) {
    WebSocketClient client = new ReactorNettyWebSocketClient();
    Void somevoid = client.execute(
        URI.create("ws://localhost:8081"), session -> session.send(
                Mono.just(
                    session.textMessage(nostrRequestString)
                ))
            .thenMany(
                session.receive()
                    .map(
                        WebSocketMessage::getPayloadAsText)
                    .log())
            .then()).block();
    System.out.println(somevoid);
    return somevoid;
  }

  public T booo(String event, Class<T> returnType) {
    WebClient webClient = WebClient.builder().filter(logRequest()).build();
    return webClient.post()
        .uri(uriBuilder -> uriBuilder
            .scheme(HTTPS)
            .host(HOST)
            .port(PORT)
            .build())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(
            BodyInserters.fromValue(nostrRequestString))
        .retrieve()
        .bodyToMono(returnType)
        .block();
  }


  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
      clientRequest.headers().forEach((name, values) -> values.forEach(value -> LOGGER.info(String.format("%s=%s", name, value))));
      return Mono.just(clientRequest);
    });
  }
}
