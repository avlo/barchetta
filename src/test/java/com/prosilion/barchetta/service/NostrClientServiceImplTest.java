package com.prosilion.barchetta.service;

import com.prosilion.barchetta.service.nostr.NostrClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.prosilion.barchetta.service.nostr.NostrClientService.nostrRequestString;

@ExtendWith(SpringExtension.class)
class NostrClientServiceImplTest<T> {
  NostrClientServiceImpl<T> nostrClientService;

  @BeforeEach
  void setup() {
    this.nostrClientService = new NostrClientServiceImpl<>();
  }

  @Test
  void myStompSessionHandlerIT() {
    nostrClientService.getClassifiedListingEvent(nostrRequestString);
  }
}