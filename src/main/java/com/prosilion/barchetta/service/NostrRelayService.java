package com.prosilion.barchetta.service;

import com.google.common.collect.Streams;
import com.prosilion.barchetta.client.StandardWebSocketClient;
import com.prosilion.barchetta.client.WebSocketClientIF;
import com.prosilion.barchetta.model.entity.Contract;
import lombok.NonNull;
import nostr.api.factory.impl.NIP01Impl.EventMessageFactory;
import nostr.event.BaseMessage;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.util.NostrException;
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
import java.util.stream.Stream;

@Service
public class NostrRelayService {
  private Map<Long, Map<String, WebSocketClientIF>> eventSocketClientMap = new ConcurrentHashMap<>();
  private Map<Long, Map<String, WebSocketClientIF>> requestSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private final SslBundles sslBundles;

  @Autowired
  public NostrRelayService(
      @Value("${nostr.relay.uri}") String relayUri,
      SslBundles sslBundles) {
    this.relayUri = relayUri;
    this.sslBundles = sslBundles;
  }

  public Contract save(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException, NostrException {
    ClassifiedListingEvent classifiedListingEvent = contract.getClassifiedListingEvent();
    CalendarTimeBasedEvent calendarTimeBasedEvent = contract.getCalendarTimeBasedEvent();

    OkMessage okMessageClassifiedListing =
        createNostrEvent(
            new EventMessageFactory(
                classifiedListingEvent,
                contract.getId().toString()
            ).create(),
            contract.getId()
        );

    OkMessage okMessageCalendarTimeBasedEvent =
        createNostrEvent(
            new EventMessageFactory(
                calendarTimeBasedEvent,
                contract.getId().toString()
            ).create(),
            contract.getId()
        );

    if (!okMessageClassifiedListing.getFlag() || !okMessageCalendarTimeBasedEvent.getFlag())
      throw new NostrException("failed OK from relay");

    return contract;
  }

  private OkMessage createNostrEvent(
      @NonNull EventMessage eventMessage,
      @NonNull Long subscriptionId) throws ExecutionException, InterruptedException, IOException {
    List<String> received = sendEvent(eventSocketClientMap, subscriptionId, eventMessage);
    Optional<String> last = Streams.findLast(received.stream());
    return last
        .map(baseMessage -> new BaseMessageDecoder<OkMessage>().decode(baseMessage))
        .orElseThrow();
  }

  public Contract get(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException {
    ClassifiedListingEvent classifiedListingEvent =
        sendNostrRequest(
            contract.getNostrClassifiedListingEventId(),
            contract.getId(),
            ClassifiedListingEvent.class
        );

    CalendarTimeBasedEvent calendarTimeBasedEvent =
        sendNostrRequest(
            contract.getNostrCalendarTimeBasedEventId(),
            contract.getId(),
            CalendarTimeBasedEvent.class
        );

    contract.setClassifiedListingEvent(classifiedListingEvent);
    contract.setCalendarTimeBasedEvent(calendarTimeBasedEvent);

    return contract;
  }

  private <T extends GenericEvent> T sendNostrRequest(
      @NonNull String eventId,
      @NonNull Long subscriberId,
      @NonNull Class<T> type) throws IOException, ExecutionException, InterruptedException {
    List<String> returnedEvents = sendRequest(requestSocketClientMap, subscriberId, eventId);

    System.out.println("55555555555555555");
    System.out.println("after REQUEST:");
    System.out.printf("key:\n  [%d], eventId: [%s]\n", subscriberId, eventId);
    System.out.println("-----------------");
    System.out.println("returnedEvents:");
    returnedEvents.forEach(event -> System.out.printf("  [%s]\n", event));
    System.out.println("55555555555555555");
    Stream<BaseMessage> baseMessageStream = returnedEvents.stream().map(baseMessage -> new BaseMessageDecoder<>().decode(baseMessage));

    Stream<BaseMessage> baseMessageStream1 = baseMessageStream.filter(baseMessage -> !baseMessage.getCommand().equalsIgnoreCase("EOSE"));

    Stream<BaseMessage> baseMessageStream2 = baseMessageStream1.filter(EventMessage.class::isInstance);

    Stream<EventMessage> eventMessageStream = baseMessageStream2.map(EventMessage.class::cast);

    Stream<GenericEvent> genericEventStream = eventMessageStream.map(eventMessage -> (GenericEvent) eventMessage.getEvent());

    Stream<String> stringStream = genericEventStream.map(event -> new BaseEventEncoder<>(event).encode());

    Stream<T> tStream = stringStream.map(encode -> new GenericEventDecoder<>(type).decode(encode));

//    Optional<T> max = tStream.max((a, b) -> {
//      System.out.println("a: " + a);
//      System.out.println("b: " + b);
//      return getCompare(a, b);
//    });
//    T latestDatedEvent = max.orElseThrow();

    List<T> list = tStream.toList();
    T last = list.getLast();
    T latestDatedEvent = last;

//    System.out.println("2222222222222222222");
//    System.out.println("2222222222222222222");
//    System.out.println(latestDatedEvent);
//    System.out.println("2222222222222222222");
//    System.out.println("2222222222222222222");
    return latestDatedEvent;
  }

  private <T extends GenericEvent> int getCompare(T a, T b) {
    Long aCreatedAt = a.getCreatedAt();
    Long bCreatedAt = b.getCreatedAt();
    int compare = Long.compare(aCreatedAt, bCreatedAt);
    return compare;
  }

  private String createReqJson(@NonNull String subscriberId, @NonNull String id) {
//    TODO: re-introduce below after investigation
//    String result = ids.stream()
//        .map(s -> "\"" + s + "\"")
//        .collect(Collectors.joining(", "));
//    String joinedString = "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[" + result + "]}]";
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
  }

  private List<String> sendEvent(Map<Long, Map<String, WebSocketClientIF>> clientIFMap, @NonNull Long key, EventMessage eventMessage) throws ExecutionException, InterruptedException, IOException {
    final Map<String, WebSocketClientIF> keyMap = clientIFMap.get(key);
    if (keyMap != null) {
      WebSocketClientIF webSocketClientIF = keyMap.get(eventMessage.getEvent().getId());
      if (webSocketClientIF != null) {
        System.out.printf("111111111111 existing EVENT socket\nkey:\n  [%d]\nsocket:\n  [%s], event: [%s]\n\n", key, webSocketClientIF.getClientSession().getId(), eventMessage.getEvent().getId());
        List<String> events = webSocketClientIF.getEvents();
        System.out.println("-------------");
        System.out.println("socket getEvents():");
        events.forEach(event -> System.out.printf("  [%s]\n", event));
        System.out.println("111111111111\n");
        return events;
      }
    }

    Map<String, WebSocketClientIF> innerMap = new ConcurrentHashMap<>();
    innerMap.put(eventMessage.getEvent().getId(), new StandardWebSocketClient(relayUri, sslBundles));

    clientIFMap.put(
        key,
        innerMap);

    final WebSocketClientIF webSocketClientIF = clientIFMap.get(key).get(eventMessage.getEvent().getId());
    System.out.printf("000000000000 new EVENT socket\nkey:\n  [%d]\neventId-Key:\n  [%s]\nsocket:\n  [%s], event: [%s]\n\n", key, eventMessage.getEvent().getId(), webSocketClientIF.getClientSession().getId(), eventMessage.getEvent().getId());
    webSocketClientIF.send(eventMessage);
    List<String> events = webSocketClientIF.getEvents();
    System.out.println("-------------");
    System.out.println("socket getEvents():");
    events.forEach(event -> System.out.printf("  [%s]\n", event));
    System.out.println("000000000000\n");
    return webSocketClientIF.getEvents();
  }

  private List<String> sendRequest(Map<Long, Map<String, WebSocketClientIF>> clientIFMap, @NonNull Long key, String eventId) throws ExecutionException, InterruptedException, IOException {
    final Map<String, WebSocketClientIF> keyMap = clientIFMap.get(key);
    if (keyMap != null) {
      WebSocketClientIF webSocketClientIF = keyMap.get(eventId);
      if (webSocketClientIF != null) {
        System.out.printf("3333333333333 existing REQ socket\nkey:\n  [%d]\nsocket:\n  [%s], event: [%s]\n\n", key, webSocketClientIF.getClientSession().getId(), eventId);
        List<String> events = webSocketClientIF.getEvents();
        System.out.println("-------------");
        System.out.println("socket getEvents():");
        events.forEach(event -> System.out.printf("  [%s]\n", event));
        System.out.println("33333333333\n");
        return events;
      }
    }

    Map<String, WebSocketClientIF> innerMap = new ConcurrentHashMap<>();
    innerMap.put(eventId, new StandardWebSocketClient(relayUri, sslBundles));

    clientIFMap.put(
        key,
        innerMap);

    final WebSocketClientIF webSocketClientIF = clientIFMap.get(key).get(eventId);
    System.out.printf("222222222222 new REQ socket\nkey:\n  [%d]\neventId-Key:\n  [%s]\nsocket:\n  [%s], event: [%s]\n\n", key, eventId, webSocketClientIF.getClientSession().getId(), eventId);
    webSocketClientIF.send(createReqJson(key.toString(), eventId));
    List<String> events = webSocketClientIF.getEvents();
    System.out.println("-------------");
    System.out.println("socket getEvents():");
    events.forEach(event -> System.out.printf("  [%s]\n", event));
    System.out.println("222222222222\n");
    return webSocketClientIF.getEvents();
  }
}
