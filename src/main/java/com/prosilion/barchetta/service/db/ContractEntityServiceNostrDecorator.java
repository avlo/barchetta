package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.client.StandardWebSocketClient;
import com.prosilion.barchetta.client.WebSocketClientIF;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.User;
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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
public class ContractEntityServiceNostrDecorator implements ContractEntityServiceNostrDecoratorIF {
  private final String relayUri;
  private final ContractEntityServiceIF contractService;
  private final Map<String, WebSocketClientIF> webSocketClientMap = new HashMap<>();

  public ContractEntityServiceNostrDecorator(ContractEntityServiceIF contractService, String relayUri) {
    this.contractService = contractService;
    this.relayUri = relayUri;
  }

  private WebSocketClientIF getSocket(String key) throws ExecutionException, InterruptedException {
    WebSocketClientIF webSocketClientIF = webSocketClientMap.putIfAbsent(key, new StandardWebSocketClient(relayUri));
    WebSocketClientIF webSocketClientIF1 = Optional.ofNullable(webSocketClientIF).orElse(webSocketClientMap.get(key));
    return webSocketClientIF1;
  }

  @Transactional
  @Override
  public Contract save(@NonNull Contract contract) throws IOException, NostrException, ExecutionException, InterruptedException {
    log.info("saving contract {}", contract);
    ClassifiedListingEvent classifiedListingEvent = contract.getClassifiedListingEvent();
    CalendarTimeBasedEvent calendarTimeBasedEvent = contract.getCalendarTimeBasedEvent();

    OkMessage okMessageClassifiedListing = createNostrEvent(
        new EventMessageFactory(classifiedListingEvent, contract.getNostrAppUserPubKey()).create(),
        contract.getNostrAppUserPubKey()
    );

    OkMessage okMessageCalendarTimeBasedEvent = createNostrEvent(
        new EventMessageFactory(calendarTimeBasedEvent, contract.getNostrAppUserPubKey()).create(),
        contract.getNostrAppUserPubKey());

    if (!okMessageClassifiedListing.getFlag() || !okMessageCalendarTimeBasedEvent.getFlag())
      throw new NostrException("failed OK from relay");

    return contractService.save(contract);
  }

  @NotNull
  private OkMessage createNostrEvent(EventMessage classifiedListingEventMessage, String pubKey) throws IOException, ExecutionException, InterruptedException {
    return getSocket(pubKey)
        .send(classifiedListingEventMessage)
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<OkMessage>().decode(baseMessage))
        .findFirst()
        .orElseThrow();
  }

  @SneakyThrows
  @Override
  public Contract getContractById(@NonNull Long id, @NonNull String pubKey) {
    Contract contractByDbId = getContractById(id);

    ClassifiedListingEvent classifiedListingEvent = sendNostrRequest(
        contractByDbId.getNostrClassifiedListingEventId(), pubKey,
        ClassifiedListingEvent.class);

    CalendarTimeBasedEvent calendarTimeBasedEvent = sendNostrRequest(
        contractByDbId.getNostrCalendarTimeBasedEventId(), pubKey,
        CalendarTimeBasedEvent.class);

    contractByDbId.setClassifiedListingEvent(classifiedListingEvent);
    contractByDbId.setCalendarTimeBasedEvent(calendarTimeBasedEvent);

    return contractByDbId;
  }

  @Override
  public Contract getContractById(@NotNull Long id) {
    return contractService.getContractById(id);
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull User user) {
    return getContractsById(contractService.getAvailableOppositeRoleContractsByAppUser(user));
  }

  @Override
  public List<Contract> getContractsByCoParty(@NonNull User user) {
    return getContractsById(contractService.getContractsByCoParty(user));
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull User user) {
    List<Contract> contractsByAppUser = contractService.getContractsByAppUser(user);
    return getContractsById(contractsByAppUser);
  }

  @NotNull
  private List<Contract> getContractsById(List<Contract> contracts) {
    return contracts.stream()
        .map(Contract::getId)
        .map(this::getContractById).toList();
  }

  @Override
  public List<Contract> getAll() {
    return contractService.getAll();
  }

  private <T extends GenericEvent> T sendNostrRequest(String eventId, String pubKey, Class<T> type) throws IOException, ExecutionException, InterruptedException {
    return getSocket(pubKey)
        .send(
            createReqJson(pubKey, eventId))
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<EventMessage>().decode(baseMessage))
        .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
        .map(event -> new BaseEventEncoder<>(event).encode())
        .map(encode -> new GenericEventDecoder<T>(type).decode(encode))
        .findFirst()
        .orElseThrow();
  }

  private String createReqJson(String subscriberId, String id) {
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
  }
}
