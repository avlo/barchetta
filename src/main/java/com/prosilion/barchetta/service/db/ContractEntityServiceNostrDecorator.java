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
  private final ContractEntityServiceIF contractEntityService;
  private final Map<String, WebSocketClientIF> webSocketClientMap = new HashMap<>();

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

//    TODO: worth keeping in mind, we're swallowing OKMessage here instead of returning to subscriber/client/UI
    OkMessage okMessageClassifiedListing = createNostrEvent(
        new EventMessageFactory(classifiedListingEvent, contract.getNostrAppUserPubKey()).create(),
        contract.getNostrAppUserPubKey()
    );

//    TODO: worth keeping in mind, we're swallowing OKMessage here instead of returning to subscriber/client/UI
    OkMessage okMessageCalendarTimeBasedEvent = createNostrEvent(
        new EventMessageFactory(calendarTimeBasedEvent, contract.getNostrAppUserPubKey()).create(),
        contract.getNostrAppUserPubKey());

    if (!okMessageClassifiedListing.getFlag() || !okMessageCalendarTimeBasedEvent.getFlag())
      throw new NostrException("failed OK from relay");

    return contractEntityService.save(contract);
  }

  @SneakyThrows
  @Override
  public Contract getContract(@NonNull Long contractId, @NonNull String pubKeySubscriptionId) {
    Contract contractByDbId = getContract(contractId);

    ClassifiedListingEvent classifiedListingEvent = sendNostrRequest(
        contractByDbId.getNostrClassifiedListingEventId(), pubKeySubscriptionId,
        ClassifiedListingEvent.class);

    CalendarTimeBasedEvent calendarTimeBasedEvent = sendNostrRequest(
        contractByDbId.getNostrCalendarTimeBasedEventId(), pubKeySubscriptionId,
        CalendarTimeBasedEvent.class);

    contractByDbId.setClassifiedListingEvent(classifiedListingEvent);
    contractByDbId.setCalendarTimeBasedEvent(calendarTimeBasedEvent);

    return contractByDbId;
  }

  @Override
  public Contract getContract(@NotNull Long id) {
    return contractEntityService.getContract(id);
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
    return populateContracts(contractEntityService.getAvailableOppositeRoleContractsByAppUser(appUser));
  }

  @Override
  public List<Contract> getContractsByCoParty(@NonNull AppUser coParty) {
    return populateContracts(contractEntityService.getContractsByCoParty(coParty));
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull AppUser appUser) {
    List<Contract> contractsByAppUser = contractEntityService.getContractsByAppUser(appUser);
    return populateContracts(contractsByAppUser);
  }

  @Override
  public List<Contract> getAllContracts(@NonNull String pubKeySubscriptionId) {
    return getAllContracts().stream()
        .map(Contract::getId)
        .map(contractId -> getContract(contractId, pubKeySubscriptionId)).toList();
  }

  @Override
  public List<Contract> getAllContracts() {
    return contractEntityService.getAllContracts();
  }

  @NotNull
  private OkMessage createNostrEvent(
      @NonNull EventMessage classifiedListingEventMessage,
      @NonNull String pubKey) throws ExecutionException, InterruptedException, IOException {
    return getSocket(pubKey)
        .send(classifiedListingEventMessage)
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<OkMessage>().decode(baseMessage))
        .findFirst()
        .orElseThrow();
  }

  private <T extends GenericEvent> T sendNostrRequest(
      @NonNull String eventId,
      @NonNull String pubKeySubscriberId,
      @NonNull Class<T> type) throws IOException, ExecutionException, InterruptedException {
    return getSocket(pubKeySubscriberId)
        .send(
            createReqJson(pubKeySubscriberId, eventId))
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<EventMessage>().decode(baseMessage))
        .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
        .map(event -> new BaseEventEncoder<>(event).encode())
        .map(encode -> new GenericEventDecoder<T>(type).decode(encode))
        .findFirst()
        .orElseThrow();
  }

  private String createReqJson(@NonNull String subscriberId, @NonNull String id) {
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
  }

  private List<Contract> populateContracts(List<Contract> contracts) {
    return contracts.stream()
        .map(Contract::getId)
        .map(this::getContract).toList();
  }

  private WebSocketClientIF getSocket(@NonNull String key) throws ExecutionException, InterruptedException {
    WebSocketClientIF webSocketClientIF = webSocketClientMap.putIfAbsent(key, new StandardWebSocketClient(relayUri));
    WebSocketClientIF webSocketClientIF1 = Optional.ofNullable(webSocketClientIF).orElse(webSocketClientMap.get(key));
    return webSocketClientIF1;
  }
}
