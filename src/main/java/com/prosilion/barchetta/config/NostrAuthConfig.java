package com.prosilion.barchetta.config;

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
public class NostrAuthConfig {

  @Bean
  @Primary
  UserServiceNostrDecoratorIF userServiceIF(UserService userService) {
    return new UserServiceNostrDecorator(userService);
  }

//  TODO: below ReactiveWebSocketClient unexpectedly closes cnxn after send(), needs investigation
//  @Bean
//  @Primary
//  ContractEntityServiceIF contractServiceIF(
//      ContractEntityService contractEntityService,
//      ReactiveWebSocketClient reactiveWebSocketClient) {
//    return new ContractEntityServiceNostrDecorator(
//        contractEntityService,
//        reactiveWebSocketClient);
//  }
}
