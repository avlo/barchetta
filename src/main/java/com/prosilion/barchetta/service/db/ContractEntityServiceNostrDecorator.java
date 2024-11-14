package com.prosilion.barchetta.service.db;

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
import java.util.List;

@Slf4j
public class ContractEntityServiceNostrDecorator implements ContractEntityServiceIF {
  private final ContractEntityServiceIF contractService;
  private final WebSocketClientIF webSocketClient;

  public ContractEntityServiceNostrDecorator(ContractEntityServiceIF contractService, WebSocketClientIF webSocketClient) {
    this.contractService = contractService;
    this.webSocketClient = webSocketClient;
  }

  @Transactional
  @Override
  public Contract save(@NonNull Contract contract) throws IOException, NostrException {
    log.info("saving contract {}", contract);
    ClassifiedListingEvent classifiedListingEvent = contract.getClassifiedListingEvent();
    CalendarTimeBasedEvent calendarTimeBasedEvent = contract.getCalendarTimeBasedEvent();

    OkMessage okMessageClassifiedListing = sendRequest(
        new EventMessageFactory(classifiedListingEvent, contract.getNostrAppUserPubKey()).create()
    );

    OkMessage okMessageCalendarTimeBasedEvent = sendRequest(
        new EventMessageFactory(calendarTimeBasedEvent, contract.getNostrAppUserPubKey()).create());

    if (!okMessageClassifiedListing.getFlag() || !okMessageCalendarTimeBasedEvent.getFlag())
      throw new NostrException("failed OK from relay");

    return contractService.save(contract);
  }

  @NotNull
  private OkMessage sendRequest(EventMessage classifiedListingEventMessage) throws IOException {
    return webSocketClient.send(classifiedListingEventMessage)
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<OkMessage>().decode(baseMessage))
        .findFirst()
        .orElseThrow();
  }

  @SneakyThrows
  @Override
  public Contract getContractById(@NonNull Long id) {
    Contract contractByDbId = contractService.getContractById(id);

    ClassifiedListingEvent classifiedListingEvent = request(
        contractByDbId.getNostrClassifiedListingEventId(),
        ClassifiedListingEvent.class);

    CalendarTimeBasedEvent calendarTimeBasedEvent = request(
        contractByDbId.getNostrCalendarTimeBasedEventId(),
        CalendarTimeBasedEvent.class);

    contractByDbId.setClassifiedListingEvent(classifiedListingEvent);
    contractByDbId.setCalendarTimeBasedEvent(calendarTimeBasedEvent);

    return contractByDbId;
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull AppUser appUser) {
    return contractService.getContractsByAppUser(appUser);
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
    return contractService.getAvailableOppositeRoleContractsByAppUser(appUser);
  }

  @Override
  public List<Contract> getContractsByCoPartyId(@NonNull Long id) {
    return contractService.getContractsByCoPartyId(id);
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUserId(@NonNull Long id) {
    return contractService.getAvailableOppositeRoleContractsByAppUserId(id);
  }

  @Override
  public List<Contract> getContractsByAppUserId(@NonNull Long id) {
    return contractService.getContractsByAppUserId(id).stream()
        .map(contract ->
            getContractById(contract.getId()))
        .toList();
  }

  @Override
  public List<Contract> getAll() {
    return contractService.getAll();
  }

  private <T extends GenericEvent> T request(String eventId, Class<T> type) throws IOException {
    return webSocketClient
        .send(
            createReqJson("NEEDS-RESOLUTION", eventId))
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
