package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.client.WebSocketClientIF;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.ContractStateEnum;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.user.UserServiceNostrDecoratorIF;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.api.factory.impl.NIP01Impl.EventMessageFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.util.NostrException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    ClassifiedListingEvent classifiedListingEvent = constructClassifiedListingEvent(contract);
    CalendarTimeBasedEvent calendarTimeBasedEvent = constructCalendarTimeBasedEvent(contract);
    String userPubKeyAsSubscriptionId = contract.getNostrAppUserPubKey();

    Identity identity = Identity.generateRandomIdentity();
    classifiedListingEvent.setSignature(identity.sign(classifiedListingEvent));
    calendarTimeBasedEvent.setSignature(identity.sign(calendarTimeBasedEvent));

    EventMessage classifiedListingEventMessage = new EventMessageFactory(classifiedListingEvent, userPubKeyAsSubscriptionId).create();
    EventMessage calendarTimeBasedEventMessage = new EventMessageFactory(calendarTimeBasedEvent, userPubKeyAsSubscriptionId).create();

    OkMessage okMessageClassifiedListing = getOkMessage(classifiedListingEventMessage);
    OkMessage okMessageCalendarTimeBasedEvent = getOkMessage(calendarTimeBasedEventMessage);

    if (!okMessageClassifiedListing.getFlag() || !okMessageCalendarTimeBasedEvent.getFlag())
      throw new NostrException("failed OK from relay");

    contract.setNostrClassifiedListingEventId(classifiedListingEvent.getId());
    contract.setNostrCalendarTimeBasedEventId(calendarTimeBasedEvent.getId());
    return contractService.save(contract);
  }

  @NotNull
  private OkMessage getOkMessage(EventMessage classifiedListingEventMessage) throws IOException {
    return webSocketClient.send(classifiedListingEventMessage)
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<OkMessage>().decode(baseMessage))
        .findFirst().get();
  }

  @SneakyThrows
  @Override
  public Contract getContractById(@NonNull Long id) {
    Contract contractByDbId = contractService.getContractById(id);
    String classifiedListingEventId = contractByDbId.getNostrClassifiedListingEventId();
    String nostrAppUserPubKey = contractByDbId.getNostrAppUserPubKey();

    User userByPubKey = contractAppUserServiceNostrDecorator.findByPubKey(nostrAppUserPubKey);
    ClassifiedListingEvent classifiedListingEvent = reqEventForPubKeyByEventIdAndType(userByPubKey.getNostrPubKey(), classifiedListingEventId, ClassifiedListingEvent.class);

    String calendarTimeBasedEventId = contractByDbId.getNostrCalendarTimeBasedEventId();
    CalendarTimeBasedEvent calendarTimeBasedEvent = reqEventForPubKeyByEventIdAndType(userByPubKey.getNostrPubKey(), calendarTimeBasedEventId, CalendarTimeBasedEvent.class);
    Contract contract = convertToContract(classifiedListingEvent, calendarTimeBasedEvent);
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

  private ClassifiedListingEvent constructClassifiedListingEvent(Contract contract) {
    ArrayList<BaseTag> baseTags = new ArrayList<>();
    baseTags.add(GenericTag.create("payer_state", 52, String.valueOf(contract.getPayerState())));
    baseTags.add(GenericTag.create("payee_state", 52, String.valueOf(contract.getPayeeState())));
    return new ClassifiedListingEvent(
        new PublicKey(contract.getNostrAppUserPubKey()),
        Kind.CLASSIFIED_LISTING,
        baseTags,
        "CLASSIFIED LISTING CONTENT",
        ClassifiedListing.builder(
                contract.getText(),
                "CLASSIFIED LISTING SUMMARY",
                new PriceTag(contract.getPayoutAmount(), "btc", "once"))
            .build());
  }

  private CalendarTimeBasedEvent constructCalendarTimeBasedEvent(Contract contract) {
    CalendarContent calendarContent = CalendarContent.builder(
            new IdentifierTag("UUID-NEEDS-COMPLETION-001"),
            contract.getText(),
            contract.getAgreedStartTime().getTime())
        .build();

//    TODO: needs design validation- give current/enclosing method is called by save(), should below always be done for:
//        a) appUser?
//    TODO: explicit add() used since contract *SHOULD* have creator's pubkey
    List<PubKeyTag> pubKeyTags = new ArrayList<>();

    pubKeyTags.add(
        new PubKeyTag(
            new PublicKey(
                contract.getNostrAppUserPubKey()),
            "ws://localhost:5555",
            contract.getCreatorRole().getCreatorRoleType()));

//    TODO: needs design validation- give current/enclosing method is called by save(), should below always be done for:
//        a) counterParty?
    Optional
        .ofNullable(
            contract.getNostrCounterPartyPubKey())
        .ifPresent(counterPartyPubKey -> pubKeyTags.add(
            new PubKeyTag(new PublicKey(counterPartyPubKey))
        ));

    calendarContent.setParticipantPubKeys(pubKeyTags);

//    TODO: POC design, needs revisit
    ArrayList<BaseTag> baseTags = new ArrayList<>();
    Optional
        .ofNullable(
//            TODO: below time needs evolution
            contract.getAgreedCompletionTime())
        .ifPresent(aDate ->
            baseTags.add(
                GenericTag.create(
                    "end",
                    52,
                    String.valueOf(aDate.getTime()))));

    CalendarTimeBasedEvent calendarTimeBasedEvent = new CalendarTimeBasedEvent(
        new PublicKey(contract.getNostrAppUserPubKey()),
        baseTags,
        "CALENDAR-EVENT CONTENT",
        calendarContent
    );
    return calendarTimeBasedEvent;
  }

  private <T extends GenericEvent> T reqEventForPubKeyByEventIdAndType(String subscriberId, String eventId, Class<T> clazz) throws IOException {
    String reqJson = createReqJson(subscriberId, eventId);
    Optional<T> first = webSocketClient.send(reqJson)
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<EventMessage>().decode(baseMessage))
        .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
        .map(event -> new BaseEventEncoder<>(event).encode())
        .map(encode -> new GenericEventDecoder<T>(clazz).decode(encode))
        .findFirst();
    T t = first.get();
    return t;
  }

  private String createReqJson(String subscriberId, String id) {
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + id + "\"]}]";
  }

  private Contract convertToContract(ClassifiedListingEvent classifiedListingEvent, CalendarTimeBasedEvent calendarTimeBasedEvent) {
    Contract contract = new Contract();

    // some contract attributes come from classifiedListingEvent...
    contract.setNostrClassifiedListingEventId(classifiedListingEvent.getId());
    contract.setNostrAppUserPubKey(classifiedListingEvent.getPubKey().toString());
    contract.setText(classifiedListingEvent.getClassifiedListing().getTitle());
    contract.setPayoutAmount(classifiedListingEvent.getClassifiedListing().getPriceTag().getNumber());

    getTagValueFromString(calendarTimeBasedEvent.getTags(), "payer_state")
        .ifPresent(state ->
            contract.setPayerState(
                ContractStateEnum.valueOf(state)));

    getTagValueFromString(calendarTimeBasedEvent.getTags(), "payee_state")
        .ifPresent(state ->
            contract.setPayeeState(
                ContractStateEnum.valueOf(state)));

    // ... and others from calendarTimeBasedEvent
    Optional.ofNullable(calendarTimeBasedEvent.getCalendarContent().getEnd())
        .ifPresent(end ->
            contract.setAgreedCompletionTime(new Date(end)));

    List<PubKeyTag> participantPubKeys = calendarTimeBasedEvent.getCalendarContent().getParticipantPubKeys();

//    TODO: explicit get() used since contract *SHOULD* have creator's pubkey.  consider adding Optional+exception handling otherwise
    contract.setNostrAppUserPubKey(
        participantPubKeys.stream().filter(appUserPubKeyTag ->
            appUserPubKeyTag.getPublicKey().toString().equals(classifiedListingEvent.getPubKey().toString())).findFirst().get().getPublicKey().toString()
    );

//    TODO: explicit get() used since contract *SHOULD* have creator's role.  consider adding Optional+exception handling otherwise
    List<PubKeyTag> pubKeyTagStream = participantPubKeys.stream().filter(appUserPubKeyTag ->
        appUserPubKeyTag.getPublicKey().toString().equals(calendarTimeBasedEvent.getPubKey().toString())).toList();

    contract.setCreatorRole(CreatorRoleEnum.valueOf(pubKeyTagStream.stream().findFirst().get().getPetName().toUpperCase()));

//    TODO: optional since contract may not yet have a counterparty
    List<PubKeyTag> pubKeyTags = participantPubKeys.stream().filter(pubKeyTag ->
        !pubKeyTag.getPublicKey().equals(classifiedListingEvent.getPubKey())).toList();

    pubKeyTags.stream().findFirst().ifPresent(pubKeyTag -> contract.setNostrCounterPartyPubKey(pubKeyTag.getPublicKey().toString()));

    return contract;
  }

  private Optional<String> getTagValueFromString(List<BaseTag> baseTags, String code) {
    List<GenericTag> genericTags = baseTags.stream()
        .filter(GenericTag.class::isInstance)
        .map(GenericTag.class::cast)
        .toList();

    List<GenericTag> genericTagStream = genericTags.stream()
        .filter(tag -> tag.getCode().equalsIgnoreCase(code)).toList();

    Optional<String> first = genericTagStream.stream()
        .filter(genericTag -> !genericTag.getAttributes().get(0).getValue().toString().equals("null"))
        .map(genericTag -> genericTag.getAttributes().get(0).getValue().toString())
        .findFirst();

    return first;
  }
}
