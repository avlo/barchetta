package com.prosilion.barchetta.model.entity;

import com.prosilion.barchetta.model.dto.CalendarTimeBasedEventDto;
import com.prosilion.barchetta.model.dto.ClassifiedListingEventDto;
import com.prosilion.barchetta.model.dto.ContractDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

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

  public Contract(ClassifiedListingEvent classifiedListingEvent, CalendarTimeBasedEvent calendarTimeBasedEvent) {
    this.classifiedListingEvent = classifiedListingEvent;
    this.calendarTimeBasedEvent = calendarTimeBasedEvent;

    this.nostrClassifiedListingEventId = classifiedListingEvent.getId();
    this.nostrCalendarTimeBasedEventId = calendarTimeBasedEvent.getId();
    this.nostrAppUserPubKey = classifiedListingEvent.getPubKey().toHexString();
    this.nostrCounterPartyPubKey = classifiedListingEvent.getPubKey().toHexString();
  }

  public String getText() {
    return classifiedListingEvent.getContent();
  }

  public CreatorRoleEnum getCreatorRole() {
    String role = calendarTimeBasedEvent.getTags().stream()
        .filter(tagsMap -> tagsMap.getCode().equals("p"))
        .map(PubKeyTag.class::cast)
        .filter(pubKeyTag -> pubKeyTag.getPublicKey().toHexString().equals(nostrAppUserPubKey))
        .map(PubKeyTag::getPetName).findFirst().orElseThrow();
    return CreatorRoleEnum.valueOf(role.toUpperCase());
  }

  public ContractDto convertToDto() {
    ClassifiedListingEventDto classifiedListingEventDto = new ClassifiedListingEventDto(
        classifiedListingEvent.getId(),
        classifiedListingEvent.getPubKey().toHexString(),
        classifiedListingEvent.getKind(),
        new Date(classifiedListingEvent.getCreatedAt()),
        classifiedListingEvent.getContent(),
// TODO: proper tags
//        classifiedListingEvent.getTags(),
        new ArrayList<>(),
        classifiedListingEvent.getSignature().toString()
    );

    CalendarTimeBasedEventDto calendarTimeBasedEventDto = new CalendarTimeBasedEventDto(
        calendarTimeBasedEvent.getId(),
        calendarTimeBasedEvent.getPubKey().toHexString(),
        calendarTimeBasedEvent.getKind(),
        new Date(calendarTimeBasedEvent.getCreatedAt()),
        calendarTimeBasedEvent.getContent(),
// TODO: proper tags
//        calendarTimeBasedEvent.getTags(),
        new ArrayList<>(),
        calendarTimeBasedEvent.getSignature().toString()
    );

    String role = calendarTimeBasedEvent.getTags().stream()
        .filter(tagsMap -> tagsMap.getCode().equals("p"))
        .map(PubKeyTag.class::cast)
        .filter(pubKeyTag -> pubKeyTag.getPublicKey().toHexString().equals(nostrAppUserPubKey))
        .map(PubKeyTag::getPetName).findFirst().orElseThrow();

    BigDecimal price = calendarTimeBasedEvent.getTags().stream()
        .filter(tagsMap -> tagsMap.getCode().equals("price"))
        .map(PriceTag.class::cast)
        .map(PriceTag::getNumber).findFirst().orElseThrow();

    return new ContractDto(
        new PubKeyTag(
            new PublicKey(nostrAppUserPubKey),
            "ws://localhost:555",
            role),
        new PriceTag(
            price,
            "BTC",
            "1"),
        classifiedListingEventDto,
        calendarTimeBasedEventDto
    );
  }
}
