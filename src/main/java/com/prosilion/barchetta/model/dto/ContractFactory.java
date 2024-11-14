package com.prosilion.barchetta.model.dto;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.ContractOld;
import com.prosilion.barchetta.model.entity.ContractStateEnum;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import lombok.NonNull;
import nostr.event.BaseTag;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.PubKeyTag;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ContractFactory {

  public static ContractOld convertToContract(@NonNull ClassifiedListingEvent classifiedListingEvent, @NonNull CalendarTimeBasedEvent calendarTimeBasedEvent) {
    ContractOld contract = new ContractOld();

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

  private static Optional<String> getTagValueFromString(List<BaseTag> baseTags, String code) {
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
