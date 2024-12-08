package com.prosilion.barchetta.service;

import com.google.common.collect.Streams;
import com.prosilion.barchetta.client.StandardWebSocketClient;
import com.prosilion.barchetta.client.WebSocketClientIF;
import com.prosilion.barchetta.model.entity.Contract;
import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.api.factory.impl.NIP01Impl.EventMessageFactory;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

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

  @SneakyThrows
  public Contract getContractEvents(@NonNull Contract contract) {
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
    List<String> send = getWebSocketClient(requestSocketClientMap, subscriberId)
        .send(
            createReqJson(subscriberId.toString(), eventId));

//    TODO: first time below is requested, it comes with EOSE.  subsequent times, it does not.  fix below accordingly
    T latestDatedEvent = send.stream().limit(send.size() - 1)

        .map(baseMessage -> new BaseMessageDecoder<EventMessage>().decode(baseMessage))
        .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
        .map(event -> new BaseEventEncoder<>(event).encode())
        .map(encode -> new GenericEventDecoder<>(type).decode(encode))
        .max(Comparator.comparingLong(T::getCreatedAt))
        .orElseThrow();

    return latestDatedEvent;
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
    WebSocketClientIF checkWebSocketClient = clientIFMap.get(key);
    if (checkWebSocketClient != null) {
      return checkWebSocketClient;
    }

    clientIFMap.put(
        key,
        new StandardWebSocketClient(relayUri, sslBundles));
    WebSocketClientIF webSocketClientIF = clientIFMap.get(key);
    return webSocketClientIF;
  }
}
