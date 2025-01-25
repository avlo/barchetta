package com.prosilion.barchetta.service.nostr;

import com.google.common.collect.Streams;
import com.prosilion.barchetta.client.StandardWebSocketClient;
import com.prosilion.barchetta.client.WebSocketClientIF;
import com.prosilion.barchetta.model.entity.Contract;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.factory.impl.NIP01Impl.EventMessageFactory;
import nostr.event.impl.CalendarRsvpEvent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.util.NostrException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class NostrRelayService {
  private final WebSocketClientIF eventSocketClient;

  private Map<String, WebSocketClientIF> requestSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  // TODO: below needs cleanup
  private SslBundles sslBundles = null;
  //  private final String subscriberIdPrefix;
  public NostrRelayService(@Value("${superconductor.relay.uri}") String relayUri) throws ExecutionException, InterruptedException {
    this.relayUri = relayUri;
    log.info("relayUri: \n{}", relayUri);
//    this.subscriberIdPrefix = subscriberIdPrefix;
//    log.info("subscriberIdPrefix: \n{}", subscriberIdPrefix);
    this.eventSocketClient = new StandardWebSocketClient(relayUri);
  }

  public NostrRelayService(
      @Value("${superconductor.relay.uri}") String relayUri,
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

  public Contract create(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException, NostrException {
    saveEvent(contract.getClassifiedListingEvent(), "ClassifiedListingEvent failed OK from relay");
    saveEvent(contract.getCalendarTimeBasedEvent(), "CalendarTimeBasedEvent failed OK from relay");
    return contract;
  }

  public Contract update(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException, NostrException {
    updateEvent(contract.getCalendarRsvpEvent(), "CalendarRsvpEvent failed OK from relay");
    return contract;
  }

  private <T extends GenericEvent> void saveEvent(@NonNull T clazz, @NonNull String failureString) throws NostrException, IOException {
    Optional.of(
        getOkMessage(
            sendEvent(
                new EventMessageFactory(clazz).create())).getFlag());
  }

  private <T extends GenericEvent> void updateEvent(@NonNull T clazz, @NonNull String failureString) throws NostrException, IOException {
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
    final String eventUuid = contract.getEventUuid();
    final String appUserPubKey = contract.getNostrAppUserPubKey();

    List<T> returnedEvents = sendRequest(
        eventUuid,
        createUnifiedJsonReq(eventUuid),
        List.of(
            (Class<T>) ClassifiedListingEvent.class,
            (Class<T>) CalendarTimeBasedEvent.class,
            (Class<T>) CalendarRsvpEvent.class)
    );

    returnedEvents.stream()
        .filter(ClassifiedListingEvent.class::isInstance)
        .map(ClassifiedListingEvent.class::cast)
        .findFirst()
        .ifPresent(contract::setClassifiedListingEvent);

    returnedEvents.stream()
        .filter(CalendarTimeBasedEvent.class::isInstance)
        .map(CalendarTimeBasedEvent.class::cast)
        .findFirst()
        .ifPresent(contract::setCalendarTimeBasedEvent);

    returnedEvents.stream()
        .filter(CalendarRsvpEvent.class::isInstance)
        .map(CalendarRsvpEvent.class::cast)
        .findFirst()
        .ifPresent(contract::setCalendarRsvpEvent);

    return contract;
  }

  public <T extends GenericEvent> List<T> sendRequest(
      @NonNull String clientUuid,
      @NonNull String reqJson,
      @NonNull List<Class<T>> clazzez) throws IOException, ExecutionException, InterruptedException {
    List<String> returnedEvents = request(clientUuid, reqJson);

    log.debug("55555555555555555");
    log.debug("after REQUEST:");
    log.debug("key:\n  [{}]\n", clientUuid);
    log.debug("-----------------");
    log.debug("returnedEvents:");
    log.debug(returnedEvents.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    log.debug("55555555555555555");

    List<String> eventsJson = returnedEvents.stream().map(baseMessage -> new BaseMessageDecoder<>().decode(baseMessage))
        .filter(EventMessage.class::isInstance)
        .map(EventMessage.class::cast)
        .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
        .sorted(Comparator.comparing(GenericEvent::getCreatedAt).reversed())
        .map(event -> new BaseEventEncoder<>(event).encode())
        .toList();

    List<Optional<T>> u = eventsJson.stream()
        .flatMap(json -> clazzez.stream()
            .map(clazz ->
                getDecode(json, clazz)))
        .toList();

    List<T> list = u.stream().filter(Optional::isPresent).map(Optional::get).toList();
    return list;
  }

  private static <T extends GenericEvent> Optional<T> getDecode(String json, Class<T> clazz) {
    try {
      T decode = new GenericEventDecoder<>(clazz).decode(json);
      return Optional.of(decode);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private String createUnifiedJsonReq(String uuid) {
    return "[\"REQ\",\"" + uuid +
        "\",{" +
//        "\"kinds\":[\"" + Kind.CALENDAR_TIME_BASED_EVENT + "\"]," +
//        "\"authors\":[\"" + pubkey + "\"]," +
        "\"#d\":[\"" + uuid + "\"]" +
        "}]";
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

    requestSocketClientMap.put(subscriberPrefixEventIdSuffix, getStandardWebSocketClient());

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

  private StandardWebSocketClient getStandardWebSocketClient() throws ExecutionException, InterruptedException {
    return Objects.nonNull(sslBundles) ? new StandardWebSocketClient(relayUri, sslBundles) :
        new StandardWebSocketClient(relayUri);
  }
}
