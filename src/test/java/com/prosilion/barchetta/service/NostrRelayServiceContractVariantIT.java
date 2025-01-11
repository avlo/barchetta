package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.service.nostr.NostrRelayService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.NIP52;
import nostr.api.NIP99;
import nostr.event.BaseTag;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.util.NostrException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext
@ActiveProfiles("test")
public class NostrRelayServiceContractVariantIT {
  Identity aliceIdentity = Identity.generateRandomIdentity();
  Identity bobIdentity = Identity.generateRandomIdentity();

  public static final String CLEVENT_CONTENT = "ClassifiedListingEvent content";
  public static final String CLEVENT_TITLE = "ClassifiedListingEvent title";
  public static final String CLEVENT_SUMMARY = "ClassifiedListingEvent summary";
  public static final String CURRENCY = "BTC";
  public static final String MONTH = "MONTH";
  public static final String LOCATION = "pangea";
  public static final PriceTag PRICE_TAG = new PriceTag(BigDecimal.valueOf(11111), CURRENCY, MONTH);

  public static final String CTBEVENT_CONTENT = "CalendarTimeBasedEvent content";
  public static final String CTBEVENT_TITLE = "CalendarTimeBasedEvent title";

  String contractId1 = "superconductor-contract_id-1";
  long aliceCreatedAt = new Date().getTime();

  private final NostrRelayService nostrRelayService;
  private Contract aliceContract;
  private Contract bobContract;

  @Autowired
  public NostrRelayServiceContractVariantIT(@NonNull NostrRelayService nostrRelayService) {
    this.nostrRelayService = nostrRelayService;
  }

  @BeforeAll
  void setup() {
    ClassifiedListingEvent clEventAlice = createAliceClassifiedListingEvent();
    CalendarTimeBasedEvent ctbEventAlice = createAliceCalendarTimeBasedEvent();

    aliceContract = new Contract(clEventAlice, ctbEventAlice, "uuid-cle-01", "uuid-ctb-02");
    aliceContract.setId(1L);

//    ClassifiedListingEvent clEventBob = ContractDto.mapJsonToEvent(createBobClassifiedListingEvent(), ClassifiedListingEvent.class);
//    CalendarTimeBasedEvent ctbEventBob = ContractDto.mapJsonToEvent(createBobCalendarTimeBasedEvent(), CalendarTimeBasedEvent.class);

//    bobContract = new Contract(clEventBob, ctbEventBob);
//    bobContract.setId(2L);
  }

  @Test
  @Order(0)
  void testSaveAliceContract() throws NostrException, IOException, ExecutionException, InterruptedException {
    aliceContract = nostrRelayService.save(aliceContract);
    aliceContract = nostrRelayService.get(aliceContract);

    aliceContract.setNostrCounterPartyPubKey(bobIdentity.getPublicKey().toHexString());
    aliceContract = nostrRelayService.save(aliceContract);
    aliceContract = nostrRelayService.get(aliceContract);
//
//    bobContract = nostrRelayService.save(bobContract);
//    bobContract = nostrRelayService.get(bobContract);
//
//    aliceContract = nostrRelayService.get(aliceContract);
//    bobContract = nostrRelayService.get(bobContract);
  }

  private ClassifiedListingEvent createAliceClassifiedListingEvent() {
    List<BaseTag> baseTags = new ArrayList<BaseTag>();

    ClassifiedListing classifiedListing = ClassifiedListing.builder(
            CLEVENT_TITLE,
            CLEVENT_SUMMARY,
            PRICE_TAG)
        .build();
    classifiedListing.setLocation(LOCATION);
    classifiedListing.setPublishedAt(aliceCreatedAt);

    return (ClassifiedListingEvent) new NIP99<>(aliceIdentity)
        .createClassifiedListingEvent(
            baseTags,
            CLEVENT_CONTENT,
            classifiedListing)
        .sign().getEvent();
  }

  private CalendarTimeBasedEvent createAliceCalendarTimeBasedEvent() {
    CalendarContent calendarContent = CalendarContent.builder(
        new IdentifierTag(contractId1),
        CTBEVENT_TITLE,
        aliceCreatedAt).build();

    List<BaseTag> tags = new ArrayList<>();
    tags.add(new PubKeyTag(aliceIdentity.getPublicKey(),
        "ws://localhost:5555",
        "ISSUER"));

    return (CalendarTimeBasedEvent) new NIP52<>(aliceIdentity)
        .createCalendarTimeBasedEvent(
            tags,
            CTBEVENT_CONTENT,
            calendarContent)
        .sign().getEvent();
  }

  private CalendarTimeBasedEvent createBobCalendarTimeBasedEvent() {
    //    tags.add(new PubKeyTag(new PublicKey("494001ac0c8af2a10f60f23538e5b35d3cdacb8e1cc956fe7a16dfa5cbfc4347"),
//        "",
//        "COUNTERPARTY"));
    return null;
  }
}
