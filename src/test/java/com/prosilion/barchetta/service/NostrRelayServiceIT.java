package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.util.NostrException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
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
  private Contract aliceContract;
  private Contract bobContract;

  @BeforeAll
  void setup() {
    nostrRelayService = new NostrRelayService(RELAY_URI, sslBundles);
    ClassifiedListingEvent clEventAlice = ContractDto.mapJsonToEvent(getAliceClassifiedListingEventJson(), ClassifiedListingEvent.class);
    CalendarTimeBasedEvent ctbEventAlice = ContractDto.mapJsonToEvent(getAliceCalendarTimeBasedEventJson(), CalendarTimeBasedEvent.class);

    aliceContract = new Contract(clEventAlice, ctbEventAlice);
    aliceContract.setId(1L);

    ClassifiedListingEvent clEventBob = ContractDto.mapJsonToEvent(getBobClassifiedListingEventJson(), ClassifiedListingEvent.class);
    CalendarTimeBasedEvent ctbEventBob = ContractDto.mapJsonToEvent(getBobCalendarTimeBasedEventJson(), CalendarTimeBasedEvent.class);

    bobContract = new Contract(clEventBob, ctbEventBob);
    bobContract.setId(2L);
  }

  @Test
  @Order(0)
  void testSaveAliceContract() throws NostrException, IOException, ExecutionException, InterruptedException {
    aliceContract = nostrRelayService.save(aliceContract);
//    System.out.println("11111111111111111");
//    System.out.println("11111111111111111");
//    System.out.println(Contract.mapEventToJson(contract.getClassifiedListingEvent()));
//    System.out.println("-----------------");
//    System.out.println(Contract.mapEventToJson(contract.getCalendarTimeBasedEvent()));
//    System.out.println("11111111111111111");
//    System.out.println("11111111111111111");

    aliceContract = nostrRelayService.get(aliceContract);
//    System.out.println("33333333333333333");
//    System.out.println("33333333333333333");
//    System.out.println(Contract.mapEventToJson(contract.getClassifiedListingEvent()));
//    System.out.println("-----------------");
//    System.out.println(Contract.mapEventToJson(contract.getCalendarTimeBasedEvent()));
//    System.out.println("33333333333333333");
//    System.out.println("33333333333333333");

//    aliceContract.setNostrCounterPartyPubKey("a7b92fd0fb2b1964e2b48712383d611086bf47920dcb792e3b383cdc545b07e5");
//    aliceContract = nostrRelayService.save(aliceContract);

//    System.out.println("44444444444444444");
//    System.out.println("44444444444444444");
//    System.out.println(Contract.mapEventToJson(contract.getClassifiedListingEvent()));
//    System.out.println("-----------------");
//    System.out.println(Contract.mapEventToJson(contract.getCalendarTimeBasedEvent()));
//    System.out.println("44444444444444444");
//    System.out.println("44444444444444444");

//    aliceContract = nostrRelayService.get(aliceContract);

//    System.out.println("55555555555555555");
//    System.out.println("55555555555555555");
//    System.out.println(Contract.mapEventToJson(contract.getClassifiedListingEvent()));
//    System.out.println("-----------------");
//    System.out.println(Contract.mapEventToJson(contract.getCalendarTimeBasedEvent()));
//    System.out.println("55555555555555555");
//    System.out.println("55555555555555555");
    bobContract = nostrRelayService.save(bobContract);
    bobContract = nostrRelayService.get(bobContract);

    aliceContract = nostrRelayService.get(aliceContract);
    bobContract = nostrRelayService.get(bobContract);
  }

  private String getAliceCalendarTimeBasedEventJson() {
    return "{\"id\":\"6eb8df291787919cf847327245e321a1795dc9b60936bb7304c9a87ac8c37698\",\"kind\":31923,\"created_at\":1733700427,\"content\":\"CTBEvent content field: aaaaaa\",\"tags\":[[\"d\",\"UUID-1733700427\"],[\"title\",\"CTBEvent title field: aaaaaa\"],[\"start\",1733800427],[\"summary\",\"CTBEvent summary field: aaaaaa\"],[\"location\",\"CTBEvent location field\"],[\"p\",\"a7b92fd0fb2b1964e2b48712383d611086bf47920dcb792e3b383cdc545b07e5\",\"wss://localhost:5555\",\"PAYER\"]],\"pubkey\":\"a7b92fd0fb2b1964e2b48712383d611086bf47920dcb792e3b383cdc545b07e5\",\"sig\":\"ed9f9844b3b1bfec598b9dbe9ee3743219f9e9b55158da126b8addaaf38ca863cad711eb0674f9224f41c08f2887e5606b1cad2e6777625e5079c91f5b02b117\"}";
  }

  private String getAliceClassifiedListingEventJson() {
    return "{\"id\":\"30e357e5801e005080774142be0a6e6b07dc4db232db88a5d4d5c201132db32d\",\"kind\":30402,\"created_at\":1733700427,\"content\":\"CLEvent content field: aaaaaa\",\"tags\":[[\"subject\",\"CLEvent subject field: aaaaaa\"],[\"title\",\"CLEvent title field: aaaaaa\"],[\"published_at\",1733700427],[\"summary\",\"CLEvent summary field: aaaaaa\"],[\"location\",\"CLEvent location field\"],[\"price\",\"1111111\",\"BTC\",\"1\"],[\"p\",\"a7b92fd0fb2b1964e2b48712383d611086bf47920dcb792e3b383cdc545b07e5\",\"wss://localhost:5555\",\"PAYER\"],[\"a\",\"31923:a7b92fd0fb2b1964e2b48712383d611086bf47920dcb792e3b383cdc545b07e5:6eb8df291787919cf847327245e321a1795dc9b60936bb7304c9a87ac8c37698\"]],\"pubkey\":\"a7b92fd0fb2b1964e2b48712383d611086bf47920dcb792e3b383cdc545b07e5\",\"sig\":\"43b2327d7321bf96e4ffa5e42b3339ca0d07613d2c1d46c8aa20e986124e1e28a929049ef6b22922eabfb415d235d14e6db0e7e39f653f6bfd9aa48fc9de1e47\"}";
  }

  private String getBobCalendarTimeBasedEventJson() {
    return "{\"id\":\"7a740de1cb26762fd7c24c003d25935d7df7579976a2fd57df8c6c003f329cd8\",\"kind\":31923,\"created_at\":1733701546,\"content\":\"CTBEvent content field: bbbbbbb\",\"tags\":[[\"d\",\"UUID-1733701546\"],[\"title\",\"CTBEvent title field: bbbbbbb\"],[\"start\",1733801546],[\"summary\",\"CTBEvent summary field: bbbbbbb\"],[\"location\",\"CTBEvent location field\"],[\"p\",\"9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224\",\"wss://localhost:5555\",\"PAYER\"]],\"pubkey\":\"9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224\",\"sig\":\"0bbf316aa13677441f9235795eb74ad82532ec59db28657843594c11f97afaddddaefc9834a9dc5f8ecdec32bb78aced888cbca8179d11b596e88f3678ff143c\"}";
  }

  private String getBobClassifiedListingEventJson() {
    return "{\"id\":\"f311c36b98fdb33b3c7462133bf603c7782c2125d63e56ac954353bf59203df0\",\"kind\":30402,\"created_at\":1733701546,\"content\":\"CLEvent content field: bbbbbbb\",\"tags\":[[\"subject\",\"CLEvent subject field: bbbbbbb\"],[\"title\",\"CLEvent title field: bbbbbbb\"],[\"published_at\",1733701546],[\"summary\",\"CLEvent summary field: bbbbbbb\"],[\"location\",\"CLEvent location field\"],[\"price\",\"2222222\",\"BTC\",\"1\"],[\"p\",\"9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224\",\"wss://localhost:5555\",\"PAYER\"],[\"a\",\"31923:9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224:7a740de1cb26762fd7c24c003d25935d7df7579976a2fd57df8c6c003f329cd8\"]],\"pubkey\":\"9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224\",\"sig\":\"43639ad55fa68b05200557fc00b6626bc3147b8eab6fd31dfd7985daf941735ebd0a95d33889e2af68c49ec97209d793456527ae22a0e3940aeccd777a772d0b\"}";
  }
}
