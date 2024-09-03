package com.prosilion.barchetta.config;

import com.prosilion.barchetta.client.NostrWebSocketClient;
import com.prosilion.barchetta.client.WebSocketClient;
import com.prosilion.barchetta.client.WebSocketHandler;
import com.prosilion.barchetta.service.ContractAppUserService;
import com.prosilion.barchetta.service.ContractAppUserServiceIF;
import com.prosilion.barchetta.service.ContractAppUserServiceNostrDecorator;
import com.prosilion.barchetta.service.ContractService;
import com.prosilion.barchetta.service.ContractServiceIF;
import com.prosilion.barchetta.service.ContractServiceNostrDecorator;
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
  ContractServiceIF contractServiceIF(ContractService contractService, NostrWebSocketClient nostrWebSocketClient) {
    return new ContractServiceNostrDecorator(contractService, nostrWebSocketClient);
  }

  @Bean
  @Primary
  ContractAppUserServiceIF contractAppUserServiceIF(ContractAppUserService contractAppUserService) {
    return new ContractAppUserServiceNostrDecorator(contractAppUserService);
  }
}
