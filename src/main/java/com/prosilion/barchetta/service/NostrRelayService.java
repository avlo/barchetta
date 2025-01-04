package com.prosilion.barchetta.service;

import com.google.common.collect.Streams;
import com.prosilion.barchetta.client.StandardWebSocketClient;
import com.prosilion.barchetta.model.entity.Contract;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.factory.impl.NIP01Impl.EventMessageFactory;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.util.NostrException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Slf4j
@Service
public class NostrRelayService {
  private final StandardWebSocketClient eventSocketClient;
  private Map<String, StandardWebSocketClient> requestSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private final String subscriberIdPrefix;

//  @Autowired
//  public NostrRelayService(
//      @Value("${superconductor.relay.uri}") String relayUri,
//      @Value("${barchetta.uuid.prefix}") String subscriberIdPrefix
//  ) throws ExecutionException, InterruptedException {
//    this.relayUri = relayUri;
//    this.subscriberIdPrefix = subscriberIdPrefix;
//    this.eventSocketClient = new StandardWebSocketClient(relayUri
//    );
//  }

  @Autowired
  public NostrRelayService(
      @Value("${superconductor.relay.uri}") String relayUri,
      @Value("${barchetta.uuid.prefix}") String subscriberIdPrefix,
      @NonNull SslBundles sslBundles
  ) throws ExecutionException, InterruptedException {
    this.relayUri = relayUri;
    this.subscriberIdPrefix = subscriberIdPrefix;
    this.eventSocketClient = new StandardWebSocketClient(relayUri, sslBundles);
  }

  public Contract save(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException, NostrException {
    saveEvent(contract.getClassifiedListingEvent(), "ClassifiedListingEvent failed OK from relay");
    saveEvent(contract.getCalendarTimeBasedEvent(), "CalendarTimeBasedEvent failed OK from relay");
    return contract;
  }

  private <T extends GenericEvent> void saveEvent(@NonNull T clazz, @NonNull String failureString) throws NostrException, IOException {
    Optional.of(
            getOkMessage(
                sendEvent(
                    new EventMessageFactory(clazz).create())).getFlag())
        .orElseThrow(() -> new NostrException(failureString));
  }

  private static OkMessage getOkMessage(@NonNull List<String> received) {
    return Streams.findLast(received.stream())
        .map(baseMessage -> new BaseMessageDecoder<OkMessage>().decode(baseMessage))
        .orElseThrow();
  }

  private List<String> sendEvent(@NonNull EventMessage eventMessage) throws IOException {
    eventSocketClient.send(eventMessage);
    log.debug("socket send EventMessage content\n  {}", eventMessage.getEvent());
    return getEvents();
  }

  public List<String> getEvents() {
    List<String> events = eventSocketClient.getEvents();
    log.info("received relay response:");
    log.info("\n" + events.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    return events;
  }

  public <T extends GenericEvent> Contract get(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException {
    contract.setClassifiedListingEvent(
        sendRequest(
            contract.getId(),
            ClassifiedListingEvent.class));

    contract.setCalendarTimeBasedEvent(
        sendRequest(
            contract.getId(),
            CalendarTimeBasedEvent.class));

    return contract;
  }

  public <T extends GenericEvent> T sendRequest(
      @NonNull Long clientUuid,
      @NonNull Class<T> clazz) throws IOException, ExecutionException, InterruptedException {
    List<String> returnedEvents = request(clientUuid);

    log.debug("55555555555555555");
    log.debug("after REQUEST:");
    log.debug("key:\n  [{}]\n", clientUuid);
    log.debug("-----------------");
    log.debug("returnedEvents:");
    log.debug(returnedEvents.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    log.debug("55555555555555555");

//    Optional<EoseMessage> eoseMessageOptional = returnedEvents.stream().map(baseMessage -> new BaseMessageDecoder<>().decode(baseMessage))
//        .filter(EoseMessage.class::isInstance)
//        .map(EoseMessage.class::cast)
//        .findFirst()
////        .map(EoseMessage::getSubscriptionId)
//        ;
    return returnedEvents.stream()
        .map(baseMessage -> new BaseMessageDecoder<>().decode(baseMessage))
        .filter(EventMessage.class::isInstance)
        .map(EventMessage.class::cast)
        .sorted(
            comparing(eventMessage ->
                ((GenericEvent) eventMessage.getEvent()).getCreatedAt()))
        .reduce((first, second) -> second) // gets last/aka, most recently dated event
        .map(clazz::cast)
        .orElseThrow();
  }

  private String createReqJson(@NonNull Long uuid) {
    final String uuidKey = Strings.concat(subscriberIdPrefix, String.valueOf(uuid));
    return "[\"REQ\",\"" + uuidKey + "\",{\"#d\":[\"" + uuidKey + "\"]}]";
  }

  private List<String> request(@NonNull Long clientUuid) throws ExecutionException, InterruptedException, IOException {
    final String subscriberPrefixEventIdSuffix = subscriberIdPrefix + clientUuid;
    final StandardWebSocketClient existingSubscriberUuidWebClient = requestSocketClientMap.get(subscriberPrefixEventIdSuffix);
    if (existingSubscriberUuidWebClient != null) {
      log.debug("3333333333333 existing REQ socket\nkey:\n  [{}]\nsocket:\n  [{}]\n\n", subscriberPrefixEventIdSuffix, existingSubscriberUuidWebClient.getClientSession().getId());
      List<String> events = existingSubscriberUuidWebClient.getEvents();
      log.debug("-------------");
      log.debug("socket getEvents():");
      events.forEach(event -> log.debug("  {}\n", event));
      log.debug("33333333333\n");
      return events;
    }

    requestSocketClientMap.put(subscriberPrefixEventIdSuffix, new StandardWebSocketClient(relayUri
//        , sslBundles
    ));

    final StandardWebSocketClient newSubscriberUuidWebClient = requestSocketClientMap.get(subscriberPrefixEventIdSuffix);
    final String newSubscriberUuidWebClientsessionId = newSubscriberUuidWebClient.getClientSession().getId();
    log.debug("222222222222 new REQ socket\nkey:\n  [{}]\nsocket:\n  [{}]\n\n", subscriberPrefixEventIdSuffix, newSubscriberUuidWebClientsessionId);
    newSubscriberUuidWebClient.send(createReqJson(clientUuid));
    List<String> events = newSubscriberUuidWebClient.getEvents();
    log.debug("-------------");
    log.debug("socket [{}] getEvents():", newSubscriberUuidWebClientsessionId);
    events.forEach(event -> log.debug("  {}\n", event));
    log.debug("222222222222\n");
    return events;
  }
}
