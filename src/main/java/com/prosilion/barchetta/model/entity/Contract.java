package com.prosilion.barchetta.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.tag.PubKeyTag;

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

  public String getPayoutAmount() {
    return classifiedListingEvent.getClassifiedListing().getPriceTag().getNumber().toPlainString();
  }

  public CreatorRoleEnum getCreatorRole() {
    String role = calendarTimeBasedEvent.getTags().stream()
        .filter(PubKeyTag.class::isInstance)
        .map(PubKeyTag.class::cast)
        .filter(pubKeyTag -> pubKeyTag.getPublicKey().toHexString().equals(nostrAppUserPubKey))
        .map(PubKeyTag::getPetName).findFirst().orElseThrow();
    return CreatorRoleEnum.valueOf(role.toUpperCase());
  }
}
