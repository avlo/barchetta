package com.prosilion.barchetta.model.entity;

import com.prosilion.barchetta.model.dto.ContractDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;

import java.math.BigDecimal;
import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Contract {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long appUserId;
  private Long counterPartyId;

  private String nostrClassifiedListingEventId;
  private String nostrCalendarTimeBasedEventId;

  private String nostrAppUserPubKey;
  private String nostrCounterPartyPubKey;

  @Transient
  private ClassifiedListingEvent classifiedListingEvent;
  @Transient
  private CalendarTimeBasedEvent calendarTimeBasedEvent;

  public Contract(
      @NonNull ClassifiedListingEvent classifiedListingEvent,
      @NonNull CalendarTimeBasedEvent calendarTimeBasedEvent) {
    this.classifiedListingEvent = classifiedListingEvent;
    this.calendarTimeBasedEvent = calendarTimeBasedEvent;

    this.nostrClassifiedListingEventId = classifiedListingEvent.getId();
    this.nostrCalendarTimeBasedEventId = calendarTimeBasedEvent.getId();
    this.nostrAppUserPubKey = classifiedListingEvent.getPubKey().toHexString();
  }

  public String getText() {
    return classifiedListingEvent.getContent();
  }

  public String getPrice() {
    return getPriceTag().getNumber().toPlainString();
  }

  private PriceTag getPriceTag() {
    return classifiedListingEvent.getClassifiedListing().getPriceTag();
  }

  public CreatorRoleEnum getCreatorRole() {
    String role = calendarTimeBasedEvent.getTags().stream()
        .filter(PubKeyTag.class::isInstance)
        .map(PubKeyTag.class::cast)
        .filter(pubKeyTag -> pubKeyTag.getPublicKey().toHexString().equals(nostrAppUserPubKey))
        .map(PubKeyTag::getPetName).findFirst().orElseThrow();
    return CreatorRoleEnum.valueOf(role.toUpperCase());
  }

  public ContractDto convertToDto() {
    String role = calendarTimeBasedEvent.getTags().stream()
        .filter(PubKeyTag.class::isInstance)
        .map(PubKeyTag.class::cast)
        .filter(pubKeyTag -> pubKeyTag.getPublicKey().toHexString().equals(nostrAppUserPubKey))
        .map(PubKeyTag::getPetName).findFirst().orElseThrow();

    BigDecimal price = getPriceTag().getNumber();

//    TODO: complete stake
    String payerStakeString = "111";
//        = classifiedListingEvent.getTags().stream()
//        .filter(GenericTag.class::isInstance)
//        .map(GenericTag.class::cast)
//        .filter(tag ->
//            tag.getCode().equalsIgnoreCase("payer_stake"))
//        .map(GenericTag::getAttributes)
//        .toList().get(0).get(1).getValue().toString();
    BigDecimal payerStake = BigDecimal.valueOf(Long.parseLong(payerStakeString));

//    TODO: complete stake
    String payeeStakeString = "222";
//        = classifiedListingEvent.getTags().stream()
//        .filter(GenericTag.class::isInstance)
//        .map(GenericTag.class::cast)
//        .filter(tag ->
//            tag.getCode().equalsIgnoreCase("payee_stake"))
//        .map(GenericTag::getAttributes)
//        .toList().get(0).get(1).getValue().toString();
    BigDecimal payeeStake = BigDecimal.valueOf(Long.parseLong(payeeStakeString));

    String completed = calendarTimeBasedEvent.getTags().stream().filter(baseTag ->
            baseTag.getCode().equalsIgnoreCase("start"))
        .filter(GenericTag.class::isInstance)
        .map(GenericTag.class::cast)
        .map(GenericTag::getAttributes)
        .map(Collection::stream)
        .findFirst().toString();
    boolean completedBoolean = Boolean.parseBoolean(completed);

    return new ContractDto(
        id,
        appUserId,
        counterPartyId,
        role,
        price,
        payerStake,
        payeeStake,
        completedBoolean,
        getPayerState(),
        getPayeeState(),
        nostrAppUserPubKey,
        nostrCounterPartyPubKey,
        mapEventToJson(classifiedListingEvent),
        mapEventToJson(calendarTimeBasedEvent),
        classifiedListingEvent.getContent());
  }

  public String getPayerState() {
    return getState("payer_state");
  }

  public ContractStateEnum getPayerStateEnum() {
    return ContractStateEnum.valueOf(getPayerState());
  }

  public String getPayeeState() {
    return getState("payee_state");
  }

  public ContractStateEnum getPayeeStateEnum() {
    return ContractStateEnum.valueOf(getPayeeState());
  }

  public String getState(String stateCode) {
    Object value = classifiedListingEvent.getTags().stream()
        .filter(baseTag ->
            baseTag.getCode().equalsIgnoreCase(stateCode))
        .findAny().map(GenericTag.class::cast)
        .orElse(
            GenericTag.create(stateCode, 52, String.valueOf(ContractStateEnum.APPROVE)))
        .getAttributes().getFirst().getValue();
    return String.valueOf(value).toUpperCase();
  }

  public static <T extends GenericEvent> String mapEventToJson(T event) {
    return new BaseEventEncoder<>(event).encode();
  }
}
