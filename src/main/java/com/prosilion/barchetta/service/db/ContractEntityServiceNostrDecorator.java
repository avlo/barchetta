package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.client.WebSocketClientIF;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.user.UserServiceNostrDecoratorIF;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.api.factory.impl.NIP01Impl.EventMessageFactory;
import nostr.base.PublicKey;
import nostr.event.Kind;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.event.tag.PriceTag;
import nostr.id.Identity;
import nostr.util.NostrException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ContractEntityServiceNostrDecorator implements ContractEntityServiceIF {
  private final ContractEntityServiceIF contractService;
  private final WebSocketClientIF webSocketClient;
  private final UserServiceNostrDecoratorIF contractAppUserServiceNostrDecorator;

  public ContractEntityServiceNostrDecorator(
      ContractEntityServiceIF contractService,
      WebSocketClientIF webSocketClient,
      UserServiceNostrDecoratorIF contractAppUserServiceNostrDecorator) {
    this.contractService = contractService;
    this.webSocketClient = webSocketClient;
    this.contractAppUserServiceNostrDecorator = contractAppUserServiceNostrDecorator;
  }

  @Transactional
  @Override
  @SneakyThrows
  public Contract save(@NonNull Contract contract) {
    log.info("saving contract {}", contract);
    ClassifiedListingEvent event = convertToClassifiedListingEvent(contract);
    String userPubKeyAsSubscriptionId = contract.getNostrAppUserPubKey();

    event.setSignature(Identity.generateRandomIdentity().sign(event));

    EventMessage eventMessage = new EventMessageFactory(event, userPubKeyAsSubscriptionId).create();

    OkMessage okMessage = webSocketClient.send(eventMessage)
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<OkMessage>().decode(baseMessage))
        .findFirst().get();

    if (!okMessage.getFlag())
      throw new NostrException("failed OK from relay");

    contract.setNostrEventId(event.getId());
    return contractService.save(contract);
  }

  @SneakyThrows
  @Override
  public Contract getContractById(@NonNull Long id) {
    Contract contractByDbId = contractService.getContractById(id);
    String contractEventId = contractByDbId.getNostrEventId();
    String nostrAppUserPubKey = contractByDbId.getNostrAppUserPubKey();
    User userByPubKey = contractAppUserServiceNostrDecorator.findByPubKey(nostrAppUserPubKey);
    ClassifiedListingEvent classifiedListingEvent = reqClassifiedEventForPubKeyByEventId(userByPubKey.getNostrPubKey(), contractEventId);
    Contract contract = convertToContract(classifiedListingEvent);
    return contract;
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull AppUser appUser) {
//    getContractsByAppUserId(appUser.getId());
    return contractService.getContractsByAppUser(appUser);
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
//    getAvailableOppositeRoleContractsByAppUserId(appUser.getId());
    return contractService.getAvailableOppositeRoleContractsByAppUser(appUser);
  }

  @Override
  public List<Contract> getContractsByCoPartyId(@NonNull Long id) {
//    getContractsByAppUserId(id);
    return contractService.getContractsByCoPartyId(id);
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUserId(@NonNull Long id) {
//    getContractsByAppUserId(id);
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
//    String allContracts = "[\"REQ\",\"" + SUBSCRIBER + "\",{\"kind\":[\"" + Kind.CLASSIFIED_LISTING + "\"]}]";
//    List.of(convertToContract(getContractByNostrAppUserId(allContracts)));
    return contractService.getAll();
  }

  private ClassifiedListingEvent convertToClassifiedListingEvent(Contract contract) {
    String nostrAppUserPubKey = contract.getNostrAppUserPubKey();
    PublicKey sender = new PublicKey(nostrAppUserPubKey);

    ClassifiedListing classifiedListing = ClassifiedListing.builder(
            contract.getText(),
            "SUMMARY",
            new PriceTag(BigDecimal.TEN, "btc", "once"))
        .build();

    ClassifiedListingEvent classifiedListingEvent = new ClassifiedListingEvent(
        sender,
        Kind.CLASSIFIED_LISTING,
        new ArrayList<>(),
        "CONTENT",
        classifiedListing);

    return classifiedListingEvent;
  }

  private ClassifiedListingEvent reqClassifiedEventForPubKeyByEventId(String subscriberId, String eventId) throws IOException {
    String reqJson = createReqJson(subscriberId, eventId);
    return webSocketClient.send(reqJson)
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<EventMessage>().decode(baseMessage))
        .map(eventMessage -> ((GenericEvent) eventMessage.getEvent()))
        .map(event -> new BaseEventEncoder<>(event).encode())
        .map(encode -> new GenericEventDecoder<>(ClassifiedListingEvent.class).decode(encode))
        .findFirst().get();
  }

  private String createReqJson(String subscriberId, String id) {
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
  }

  private Contract convertToContract(ClassifiedListingEvent classifiedListingEvent) {
    Contract contract = new Contract();
    contract.setNostrEventId(classifiedListingEvent.getId());
    contract.setNostrAppUserPubKey(classifiedListingEvent.getPubKey().toString());
    contract.setText(classifiedListingEvent.getClassifiedListing().getTitle());
    return contract;
  }
}
