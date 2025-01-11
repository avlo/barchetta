package com.prosilion.barchetta.service.nostr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Streams;
import com.prosilion.barchetta.client.StandardWebSocketClient;
import com.prosilion.barchetta.client.WebSocketClientIF;
import com.prosilion.barchetta.model.entity.Contract;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.factory.impl.NIP01Impl.EventMessageFactory;
import nostr.event.BaseMessage;
import nostr.event.Kind;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.util.NostrException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

@Slf4j
@Service
public class NostrRelayService {
  private final WebSocketClientIF eventSocketClient;

  private Map<String, WebSocketClientIF> requestSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private final SslBundles sslBundles;
//  private final String subscriberIdPrefix;

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
      SslBundles sslBundles
  ) throws ExecutionException, InterruptedException {
    this.relayUri = relayUri;
    log.info("relayUri: \n{}", relayUri);
//    this.subscriberIdPrefix = subscriberIdPrefix;
//    log.info("subscriberIdPrefix: \n{}", subscriberIdPrefix);
    this.sslBundles = sslBundles;
    log.info("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.info("sslBundles name: \n{}", server);
    log.info("sslBundles key: \n{}", server.getKey());
    log.info("sslBundles protocol: \n{}", server.getProtocol());
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
                new EventMessageFactory(clazz).create())).getFlag());
  }

  private static OkMessage getOkMessage(@NonNull List<String> received) throws NostrException {
    return Streams.findLast(received.stream())
        .map(baseMessage -> new BaseMessageDecoder<OkMessage>().decode(baseMessage))
        .orElseThrow(NostrException::new);
  }

  private List<String> sendEvent(@NonNull EventMessage eventMessage) throws IOException {
    eventSocketClient.send(eventMessage);
    log.debug("socket send EventMessage content\n  {}", eventMessage.getEvent());
    return getEvents();
  }

  public List<String> getEvents() {
    List<String> events = eventSocketClient.getEvents();
    log.debug("received relay response:");
    log.debug("\n" + events.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    return events;
  }

  public <T extends GenericEvent> Contract get(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException {
    Optional.of(
        sendRequest(
            contract.getCtbEventUuid(),
            createReqCtbEventJson(contract.getCtbEventUuid()),
            CalendarTimeBasedEvent.class)
    ).orElseGet(
        Optional::empty
    ).ifPresent(
        contract::setCalendarTimeBasedEvent);

    Optional.of(
        sendRequest(
            contract.getClEventUuid(),
            createReqClEventJson(
                contract.getClEventUuid(),
                contract.getNostrAppUserPubKey(),
                contract.getNostrCalendarTimeBasedEventId()),
            ClassifiedListingEvent.class)
    ).orElseGet(
        Optional::empty
    ).ifPresent(
        contract::setClassifiedListingEvent);

    return contract;
  }

  public <T extends GenericEvent> Optional<T> sendRequest(
      @NonNull String clientUuid,
      @NonNull String reqJson,
      @NonNull Class<T> clazz) throws IOException, ExecutionException, InterruptedException {
    List<String> returnedEvents = request(clientUuid, reqJson);

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

    Optional<String> eventJson = returnedEvents.stream().map(baseMessage -> new BaseMessageDecoder<>().decode(baseMessage))
        .filter(EventMessage.class::isInstance)
        .map(EventMessage.class::cast)
        .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
        .sorted(Comparator.comparing(GenericEvent::getCreatedAt))
        .map(event -> new BaseEventEncoder<>(event).encode())
        .reduce((first, second) -> second);

    Optional<T> t = eventJson.stream().findFirst().map(json -> new GenericEventDecoder<>(clazz).decode(json));
    return t;
  }

  private String createReqCtbEventJson(@NonNull String uuid) {
//    final String uuidKey = Strings.concat(subscriberIdPrefix, String.valueOf(uuid));
    final String uuidKey = uuid;
    return "[\"REQ\",\"" + uuidKey + "\",{\"#d\":[\"" + uuidKey + "\"]}]";
  }

  private String createReqClEventJson(String uuid, String pubkey, String eventId) {
//    final String uuidKey = Strings.concat(subscriberIdPrefix, String.valueOf(uuid));
    final String uuidKey = uuid;
    final String searchStr = String.join(":", Kind.CALENDAR_TIME_BASED_EVENT.toString(), pubkey, eventId);
    return "[\"REQ\",\"" + uuidKey + "\",{\"#a\":[\"" + searchStr + "\"]}]";
  }

  private List<String> request(String clientUuid, String reqJson) throws ExecutionException, InterruptedException, IOException {
//    final String subscriberPrefixEventIdSuffix = subscriberIdPrefix + clientUuid;
    final String subscriberPrefixEventIdSuffix = clientUuid;
    final WebSocketClientIF existingSubscriberUuidWebClient = requestSocketClientMap.get(subscriberPrefixEventIdSuffix);
    if (existingSubscriberUuidWebClient != null) {
      log.debug("3333333333333 existing REQ socket\nkey:\n  [{}]\nsocket:\n  [{}]\n\n", subscriberPrefixEventIdSuffix, existingSubscriberUuidWebClient.getClientSession().getId());
      List<String> events = existingSubscriberUuidWebClient.getEvents();
      log.debug("-------------");
      log.debug("socket getEvents():");
      events.forEach(event -> log.debug("  {}\n", event));
      log.debug("33333333333\n");
      return events;
    }

    requestSocketClientMap.put(subscriberPrefixEventIdSuffix, new StandardWebSocketClient(relayUri, sslBundles));

    final WebSocketClientIF newSubscriberUuidWebClient = requestSocketClientMap.get(subscriberPrefixEventIdSuffix);
    final String newSubscriberUuidWebClientsessionId = newSubscriberUuidWebClient.getClientSession().getId();
    log.debug("222222222222 new REQ socket\nkey:\n  [{}]\nsocket:\n  [{}]\n\n", subscriberPrefixEventIdSuffix, newSubscriberUuidWebClientsessionId);
    newSubscriberUuidWebClient.send(reqJson);
    List<String> events = newSubscriberUuidWebClient.getEvents();
    log.debug("-------------");
    log.debug("socket [{}] getEvents():", newSubscriberUuidWebClientsessionId);
    events.forEach(event -> log.debug("  {}\n", event));
    log.debug("222222222222\n");
    return events;
  }
}
