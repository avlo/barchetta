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
  private Map<Long, WebSocketClientIF> eventSocketClientMap = new ConcurrentHashMap<>();
  private Map<Long, WebSocketClientIF> requestSocketClientMap = new ConcurrentHashMap<>();
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
    List<String> received = getWebSocketClient(eventSocketClientMap, subscriptionId).send(eventMessage);
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
    WebSocketClientIF webSocketClient = getWebSocketClient(requestSocketClientMap, subscriberId);

    List<String> send = webSocketClient
        .send(
            createReqJson(subscriberId.toString(), eventId));

    Stream<BaseMessage> baseMessageStream = send.stream().map(baseMessage -> new BaseMessageDecoder<>().decode(baseMessage));

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

    System.out.println("2222222222222222222");
    System.out.println("2222222222222222222");
    System.out.println(latestDatedEvent);
    System.out.println("2222222222222222222");
    System.out.println("2222222222222222222");
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

  private WebSocketClientIF getWebSocketClient(Map<Long, WebSocketClientIF> clientIFMap, @NonNull Long key) throws ExecutionException, InterruptedException {
    final WebSocketClientIF checkWebSocketClient = clientIFMap.get(key);
    if (checkWebSocketClient != null) {
      return checkWebSocketClient;
    }

    clientIFMap.put(
        key,
        new StandardWebSocketClient(relayUri, sslBundles));
    final WebSocketClientIF webSocketClientIF = clientIFMap.get(key);
    return webSocketClientIF;
  }
}
