package com.prosilion.barchetta.config;

import com.prosilion.barchetta.client.NostrWebSocketClient;
import com.prosilion.barchetta.client.WebSocketClient;
import com.prosilion.barchetta.client.WebSocketHandler;
import com.prosilion.barchetta.service.db.ContractEntityService;
import com.prosilion.barchetta.service.db.ContractEntityServiceIF;
import com.prosilion.barchetta.service.db.ContractEntityServiceNostrDecorator;
import com.prosilion.barchetta.service.user.UserService;
import com.prosilion.barchetta.service.user.UserServiceNostrDecorator;
import com.prosilion.barchetta.service.user.UserServiceNostrDecoratorIF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(
    prefix = "barchetta",
    name = "nostr.active",
    havingValue = "true")
public class NostrConfig {

  @Bean
  WebSocketHandler webSocketHandler() {
    return new WebSocketHandler();
  }

  @Bean
  WebSocketClient webSocketClient(WebSocketHandler webSocketHandler, @Value("${nostr.relay.uri}") String relayUri) {
    return new WebSocketClient(webSocketHandler, relayUri);
  }

  @Bean
  NostrWebSocketClient nostrWebSocketClient(WebSocketClient webSocketClient) {
    return new NostrWebSocketClient(webSocketClient);
  }

  @Bean
  @Primary
  UserServiceNostrDecoratorIF userServiceIF(UserService userService) {
    return new UserServiceNostrDecorator(userService);
  }

  @Bean
  @Primary
  ContractEntityServiceIF contractServiceIF(
      ContractEntityService contractEntityService,
      NostrWebSocketClient nostrWebSocketClient,
      UserServiceNostrDecoratorIF userServiceNostrDecoratorIF) {
    return new ContractEntityServiceNostrDecorator(
        contractEntityService,
        nostrWebSocketClient,
        userServiceNostrDecoratorIF);
  }
}
