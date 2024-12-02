package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.client.StandardWebSocketClient;
import com.prosilion.barchetta.client.WebSocketClientIF;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Slf4j
public class ContractEntityServiceNostrDecorator implements ContractEntityServiceNostrDecoratorIF {
  private final String relayUri;
  private final ContractEntityServiceIF contractEntityService;
  private final Map<Long, WebSocketClientIF> subscriberIdSocketClientMap = new ConcurrentHashMap<>();

  public ContractEntityServiceNostrDecorator(
      @NonNull ContractEntityServiceIF contractEntityService,
      @NonNull String relayUri) {
    this.contractEntityService = contractEntityService;
    this.relayUri = relayUri;
  }

  @Transactional
  @Override
  public Contract save(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException, NostrException {
    log.info("saving contract {}", contract);
    ClassifiedListingEvent classifiedListingEvent = contract.getClassifiedListingEvent();
    CalendarTimeBasedEvent calendarTimeBasedEvent = contract.getCalendarTimeBasedEvent();

    Contract savedContract = contractEntityService.save(contract);

    OkMessage okMessageClassifiedListing = createNostrEvent(
        new EventMessageFactory(classifiedListingEvent, savedContract.getId().toString()).create(),
        savedContract.getId()
    );

    OkMessage okMessageCalendarTimeBasedEvent = createNostrEvent(
        new EventMessageFactory(calendarTimeBasedEvent, savedContract.getId().toString()).create(),
        savedContract.getId());

    if (!okMessageClassifiedListing.getFlag() || !okMessageCalendarTimeBasedEvent.getFlag())
      throw new NostrException("failed OK from relay");

    return contractEntityService.save(contract);
  }

  private OkMessage createNostrEvent(
      @NonNull EventMessage eventMessage,
      @NonNull Long subscriptionId) throws ExecutionException, InterruptedException, IOException {
//  TODO: are there (existing/superconductor/etc) use cases with:
//    1) a client both generating events AND requesting events?
//        i would think- yes
//
//  TODO: currently using contract.getId() as subscriptionId for event *creation*- which:
//    2) consider using a general/global barchetta ID for event *creation* since
//    1) may be superfluous, as only this class/decorator does anything with OkResponse
    return getSocket(subscriptionId)
        .send(eventMessage)
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<OkMessage>().decode(baseMessage))
        .findFirst()
        .orElseThrow();
  }

  @SneakyThrows
  @Override
  public Contract getContract(@NonNull Contract contract) {
    ClassifiedListingEvent classifiedListingEvent = sendNostrRequest(
        contract.getNostrClassifiedListingEventId(), contract.getId(),
        ClassifiedListingEvent.class);

    CalendarTimeBasedEvent calendarTimeBasedEvent = sendNostrRequest(
        contract.getNostrCalendarTimeBasedEventId(), contract.getId(),
        CalendarTimeBasedEvent.class);

    contract.setClassifiedListingEvent(classifiedListingEvent);
    contract.setCalendarTimeBasedEvent(calendarTimeBasedEvent);

    return contract;
  }

  @Override
  public Contract getContract(@NonNull Long id) {
    return getContract(contractEntityService.getContract(id));
  }

  @SneakyThrows
  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
    return contractEntityService.getAvailableOppositeRoleContractsByAppUser(appUser).stream()
        .map(this::getContract).toList();
  }

  @Override
  public List<Contract> getContractsByCoParty(@NonNull AppUser coParty) {
    return contractEntityService.getContractsByCoParty(coParty).stream()
        .map(this::getContract).toList();
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull AppUser appUser) {
    return contractEntityService.getContractsByAppUser(appUser).stream()
        .map(this::getContract).toList();
  }

  @Override
  public List<Contract> getAllContracts() {
    return contractEntityService.getAllContracts().stream()
        .map(this::getContract).toList();
  }


  //  TODO: consider refactoring below method to populate a data structure containing events returned by websocket request responses
  private <T extends GenericEvent> T sendNostrRequest(
      @NonNull String eventId,
      @NonNull Long subscriberId,
      @NonNull Class<T> type) throws IOException, ExecutionException, InterruptedException {
    T firstEvent = getSocket(subscriberId)
        .send(
            createReqJson(subscriberId.toString(), eventId)).stream()
        .findFirst()
        .map(baseMessage -> new BaseMessageDecoder<EventMessage>().decode(baseMessage))
        .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
        .map(event -> new BaseEventEncoder<>(event).encode())
        .map(encode -> new GenericEventDecoder<>(type).decode(encode)).stream()
        .findFirst()
        .orElseThrow();
    return firstEvent;
  }

  private String createReqJson(@NonNull String subscriberId, @NonNull String id) {
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
  }

  private WebSocketClientIF getSocket(@NonNull Long key) throws ExecutionException, InterruptedException {
    return Optional.ofNullable(
            subscriberIdSocketClientMap.putIfAbsent(
                key,
                new StandardWebSocketClient(relayUri)))
        .orElse(subscriberIdSocketClientMap.get(key));
  }
}
