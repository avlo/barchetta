package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.service.nostr.BarchettaNostrRelayService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.NIP52;
import nostr.api.NIP99;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.NIP52Event;
import nostr.event.NIP99Event;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CalendarRsvpContent;
import nostr.event.impl.CalendarRsvpEvent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.util.NostrException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * note: log.debug() full event/JSON avail by activating:
 * 1) lombok @ToString in @see Contract
 * 2) logging.level.com.prosilion.barchetta=debug in application-test.properties
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public class BarchettaNostrRelayServiceContractVariantIT {
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
  public static final String RSVPEVENT_CONTENT = "CalendarRsvpEvent content";

  private final String uuid = "uuid-001";
  private final long aliceCreatedAt = new Date().getTime();

  private final BarchettaNostrRelayService barchettaNostrRelayService;
  private final String relayUri;
  private Contract aliceContract;
  private Contract bobContract;

  @Autowired
  BarchettaNostrRelayServiceContractVariantIT(
      @NonNull BarchettaNostrRelayService barchettaNostrRelayService,
      @Value("${superconductor.relay.uri}") String relayUri) {
    log.debug("barchettaNostrRelayService: {}", barchettaNostrRelayService);
    log.debug("relayUri {} ", relayUri);
    this.barchettaNostrRelayService = barchettaNostrRelayService;
    this.relayUri = relayUri;
  }

  @BeforeAll
  void setup() {
    ClassifiedListingEvent clEventAlice = createAliceClassifiedListingEvent();
    CalendarTimeBasedEvent ctbEventAlice = createAliceCalendarTimeBasedEvent();

    aliceContract = new Contract(clEventAlice, ctbEventAlice, uuid);
    aliceContract.setId(1L);

//    ClassifiedListingEvent clEventBob = ContractDto.mapJsonToEvent(createBobClassifiedListingEvent(), ClassifiedListingEvent.class);
//    CalendarTimeBasedEvent ctbEventBob = ContractDto.mapJsonToEvent(createBobCalendarTimeBasedEvent(), CalendarTimeBasedEvent.class);

//    bobContract = new Contract(clEventBob, ctbEventBob);
//    bobContract.setId(2L);
  }

  @Test
  @Order(0)
  void testCreateAliceContract() throws NostrException, IOException, ExecutionException, InterruptedException {
    Contract createdAliceContract = barchettaNostrRelayService.create(aliceContract);
    log.debug("createdAliceContract contents:\n  {}\n", createdAliceContract.toString());

    assertEquals(
        aliceContract.getNostrClassifiedListingEventId(),
        createdAliceContract.getClassifiedListingEvent().getId());

    assertEquals(
        aliceContract.getClassifiedListingEvent().getPubKey().toHexString(),
        aliceIdentity.getPublicKey().toHexString());

    assertEquals(
        aliceContract.getNostrCalendarTimeBasedEventId(),
        createdAliceContract.getCalendarTimeBasedEvent().getId());

    assertEquals(
        aliceContract.getCalendarTimeBasedEvent().getPubKey().toHexString(),
        aliceIdentity.getPublicKey().toHexString());

    Contract returnedAliceContract = barchettaNostrRelayService.get(createdAliceContract);
    log.debug("returnedAliceContract contents:\n  {}\n", returnedAliceContract.toString());
//    assertTrue(
//        assertThrows(IllegalArgumentException.class, () -> new BaseMessageDecoder<>().decode(kindTarget.apply(-1)))
//            .getMessage().contains("Kind must be between 0 and 65535 but was [-1]"));

//    assertEquals("location ipsum", genericTags.stream()
//        .filter(tag -> tag.getCode().equalsIgnoreCase("location")).map(GenericTag::getAttributes).toList().getFirst().getFirst().getValue());

//    aliceContract.setNostrCounterPartyPubKey(bobIdentity.getPublicKey().toHexString());
//    aliceContract = barchettaNostrRelayService.save(aliceContract);
//
//    bobContract = barchettaNostrRelayService.save(bobContract);
//    bobContract = barchettaNostrRelayService.get(bobContract);
//
//    aliceContract = barchettaNostrRelayService.get(aliceContract);
//    bobContract = barchettaNostrRelayService.get(bobContract);
  }


  @Test
  @Order(1)
  void testCreateBobAsCounterPartyOnAliceContract() throws NostrException, IOException, ExecutionException, InterruptedException {
    Contract getAliceContract = barchettaNostrRelayService.get(aliceContract);
    log.debug("getAliceContract contents:\n  {}\n", getAliceContract.toString());

    CalendarRsvpEvent rsvpContentBob = createBobRsvpEvent();
    log.debug("rsvpContentBob contents:\n  {}\n", rsvpContentBob);

    getAliceContract.setNostrCounterPartyPubKey(bobIdentity.getPublicKey().toHexString());
    getAliceContract.setCalendarRsvpEvent(rsvpContentBob);
    Contract updatedAliceContractWBobCounterParty = barchettaNostrRelayService.update(getAliceContract);
    log.debug("updatedAliceContractWBobCounterParty contents:\n  {}\n", updatedAliceContractWBobCounterParty.toString());

    Contract getAliceContractWBobCounterParty = barchettaNostrRelayService.get(updatedAliceContractWBobCounterParty);
    log.debug("getAliceContractWBobCounterParty contents:\n  {}\n", getAliceContractWBobCounterParty.toString());
//
//    bobContract = barchettaNostrRelayService.save(bobContract);
//    bobContract = barchettaNostrRelayService.get(bobContract);
//
//    aliceContract = barchettaNostrRelayService.get(aliceContract);
//    bobContract = barchettaNostrRelayService.get(bobContract);
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

    NIP99<NIP99Event> classifiedListingEvent = new NIP99<>(aliceIdentity)
        .createClassifiedListingEvent(
            baseTags,
            CLEVENT_CONTENT,
            classifiedListing);
    classifiedListingEvent.addTag(new IdentifierTag(uuid));

    return (ClassifiedListingEvent) classifiedListingEvent
        .sign().getEvent();
  }

  private CalendarTimeBasedEvent createAliceCalendarTimeBasedEvent() {
    CalendarContent calendarContent = CalendarContent.builder(
        new IdentifierTag(uuid),
        CTBEVENT_TITLE,
        aliceCreatedAt).build();

    List<BaseTag> tags = new ArrayList<>();
    tags.add(new PubKeyTag(aliceIdentity.getPublicKey(),
        relayUri,
        "ISSUER"));

    NIP52<NIP52Event> calendarTimeBasedEvent = new NIP52<>(aliceIdentity)
        .createCalendarTimeBasedEvent(
            tags,
            CTBEVENT_CONTENT,
            calendarContent);

    return (CalendarTimeBasedEvent) calendarTimeBasedEvent
        .sign().getEvent();
  }

  private CalendarRsvpEvent createBobRsvpEvent() {
    IdentifierTag identifierTag = new IdentifierTag(uuid);
    AddressTag addressTag = new AddressTag(
        Kind.CALENDAR_TIME_BASED_EVENT.getValue(),
        aliceIdentity.getPublicKey(),
        identifierTag,
        new Relay(relayUri));

    CalendarRsvpContent rsvpContent = CalendarRsvpContent.builder(
        identifierTag,
        addressTag,
        "APPROVE").build();

    List<BaseTag> tags = new ArrayList<>();
    tags.add(new PubKeyTag(bobIdentity.getPublicKey(),
        relayUri,
        "COUNTERPARTY"));

    NIP52<NIP52Event> calendarRsvpEvent = new NIP52<>(bobIdentity)
        .createCalendarRsvpEvent(
            tags,
            RSVPEVENT_CONTENT,
            rsvpContent);

    return (CalendarRsvpEvent) calendarRsvpEvent.sign().getEvent();
  }
}
