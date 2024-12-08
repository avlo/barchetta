package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.util.NostrException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class NostrRelayServiceIT {
  private static final String RELAY_URI = "wss://localhost:5555";

  @Autowired
  SslBundles sslBundles;

  private NostrRelayService nostrRelayService;
  private Contract contract;

  @BeforeAll
  void setup() {
    nostrRelayService = new NostrRelayService(RELAY_URI, sslBundles);
    ClassifiedListingEvent clEvent = ContractDto.mapJsonToEvent(getClassifiedListingEventJson(), ClassifiedListingEvent.class);
    CalendarTimeBasedEvent ctbEvent = ContractDto.mapJsonToEvent(getCalendarTimeBasedEventJson(), CalendarTimeBasedEvent.class);

    contract = new Contract(clEvent, ctbEvent);
    contract.setId(1L);
  }

  @Test
  void testSave() throws NostrException, IOException, ExecutionException, InterruptedException {
    Contract saved = nostrRelayService.save(contract);
    System.out.println(saved);
  }

  private String getCalendarTimeBasedEventJson() {
    return "{\"id\":\"526fc0cd3183894e9f07044f3678a157886ce2cabdd0473245ae43e7efa57fe9\",\"kind\":31923,\"created_at\":1733609927,\"content\":\"CTBEvent content field: bbbbbbb\",\"tags\":[[\"d\",\"UUID-1733609927\"],[\"title\",\"CTBEvent title field: bbbbbbb\"],[\"start\",1733709927],[\"summary\",\"CTBEvent summary field: bbbbbbb\"],[\"location\",\"CTBEvent location field\"],[\"p\",\"9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224\",\"wss://localhost:5555\",\"PAYER\"]],\"pubkey\":\"9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224\",\"sig\":\"ef8c14b5901d2334d4cb3a70d344a19373e77e50445539377019ee96be5e605fed59827958511d2f41292e1d50bda836a5bd999681b46a79bb8497d0490e5bfa\"}";
  }

  private String getClassifiedListingEventJson() {
    return "{\"id\":\"322bab198c7c220b57de0602ae789d078446fe1fe8cba6e4b242a10f48219185\",\"kind\":30402,\"created_at\":1733609927,\"content\":\"CLEvent content field: bbbbbbb\",\"tags\":[[\"subject\",\"CLEvent subject field: bbbbbbb\"],[\"title\",\"CLEvent title field: bbbbbbb\"],[\"published_at\",1733609927],[\"summary\",\"CLEvent summary field: bbbbbbb\"],[\"location\",\"CLEvent location field\"],[\"price\",\"22222\",\"BTC\",\"1\"],[\"p\",\"9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224\",\"wss://localhost:5555\",\"PAYER\"],[\"a\",\"31923:9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224:526fc0cd3183894e9f07044f3678a157886ce2cabdd0473245ae43e7efa57fe9\"]],\"pubkey\":\"9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224\",\"sig\":\"1820db2d8d359521071a1e64eb107b6cb4eab1c2eec60c69c4dc147921c037a7cc572f680e32f7dbbd21db8947b4e1ed3d581d6b434056386c570604b0eb3b1a\"}";
  }
}
