package com.prosilion.scdecisionmatrix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.messaging.Message;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.ExecutionException;

import static com.prosilion.scdecisionmatrix.service.nostr.NostrClientService.nostrRequestString;

@ExtendWith(SpringExtension.class)
class MyStompSessionHandlerTest<T> implements StompListener<T> {

  MyStompSessionHandler<T> myStompSessionHandler;

  @BeforeEach
  void setup() {
    this.myStompSessionHandler = new MyStompSessionHandler<>();
  }

  @Test
  void myStompSessionHandlerIT() throws ExecutionException, InterruptedException {
    myStompSessionHandler.getMessage("ws://localhost:8081/", nostrRequestString, this);
  }

  @Override
  public void getReturnVal(Message<T> message) {
    System.out.println("!111111111111111111111");
    System.out.println(message);
  }
}