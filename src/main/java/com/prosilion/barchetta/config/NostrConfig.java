package com.prosilion.barchetta.config;

import com.prosilion.barchetta.client.NettyWebSocketClient;
import com.prosilion.barchetta.service.db.ContractEntityService;
import com.prosilion.barchetta.service.db.ContractEntityServiceIF;
import com.prosilion.barchetta.service.db.ContractEntityServiceNostrDecorator;
import com.prosilion.barchetta.service.user.UserService;
import com.prosilion.barchetta.service.user.UserServiceNostrDecorator;
import com.prosilion.barchetta.service.user.UserServiceNostrDecoratorIF;
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
  @Primary
  UserServiceNostrDecoratorIF userServiceIF(UserService userService) {
    return new UserServiceNostrDecorator(userService);
  }

  @Bean
  @Primary
  ContractEntityServiceIF contractServiceIF(
      ContractEntityService contractEntityService,
      NettyWebSocketClient nettyWebSocketClient,
      UserServiceNostrDecoratorIF userServiceNostrDecoratorIF) {
    return new ContractEntityServiceNostrDecorator(
        contractEntityService,
        nettyWebSocketClient,
        userServiceNostrDecoratorIF);
  }
}
