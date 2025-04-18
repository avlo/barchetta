package com.prosilion.barchetta.service.nostr;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.subdivisions.service.NostrRelayService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.filter.Filters;
import nostr.event.filter.IdentifierTagFilter;
import nostr.event.impl.CalendarRsvpEvent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.IdentifierTag;
import nostr.util.NostrException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class BarchettaNostrRelayService {
  private final NostrRelayService nostrRelayService;

  private final String relayUri;
  // TODO: below needs cleanup
  private SslBundles sslBundles = null;

  public BarchettaNostrRelayService(@Value("${superconductor.relay.uri}") String relayUri) throws ExecutionException, InterruptedException {
    this.relayUri = relayUri;
    log.info("relayUri: \n{}", relayUri);
    this.nostrRelayService = new NostrRelayService(relayUri);
    System.out.println("11111111111111111111");
  }

  public BarchettaNostrRelayService(
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
    this.nostrRelayService = new NostrRelayService(relayUri);
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

  private <T extends GenericEvent> void saveEvent(@NonNull T event, @NonNull String failureString) throws NostrException, IOException {
    nostrRelayService.sendEvent(new EventMessage(event));
  }

  private <T extends GenericEvent> void updateEvent(@NonNull T event, @NonNull String failureString) throws NostrException, IOException {
    nostrRelayService.sendEvent(new EventMessage(event));
  }

  public <T extends GenericEvent> Contract get(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException {
    final String eventUuid = contract.getEventUuid();
    final String appUserPubKey = contract.getNostrAppUserPubKey();

    ReqMessage reqMessage = new ReqMessage(
            appUserPubKey,
            new Filters(
                    new IdentifierTagFilter<>(new IdentifierTag(eventUuid))));

    List<T> returnedEvents = sendRequest(reqMessage,
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

  private <T extends GenericEvent> List<T> sendRequest(ReqMessage reqMessage, List<Class<T>> clazzez) throws IOException {
    List<GenericEvent> returnedEvents = request(reqMessage);

    log.debug("55555555555555555");
    log.debug("after REQUEST:");
    log.debug("key:\n  [{}]\n", reqMessage.getSubscriptionId());
    log.debug("-----------------");
    log.debug("returnedEvents:");
    log.debug(returnedEvents.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    log.debug("55555555555555555");

    List<String> eventsJson = returnedEvents.stream()
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

  private List<GenericEvent> request(ReqMessage reqMessage) throws IOException {
    return nostrRelayService.sendRequestReturnEvents(reqMessage);
  }
}
