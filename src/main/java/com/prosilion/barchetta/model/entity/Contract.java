package com.prosilion.barchetta.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

  private String nostrClassifiedListingEventId; // done
  private String nostrCalendarTimeBasedEventId;

  private String nostrAppUserPubKey;  // done
  private String nostrCounterPartyPubKey; // done, needs correctness confirmation

  private String text; // done
  private BigDecimal payerStake;
  private BigDecimal payeeStake;
  private BigDecimal payoutAmount; // done

  private Boolean completed;
  private Date agreedStartTime;
  private Date agreedCompletionTime; // done

  private CreatorRoleEnum creatorRole; // done
  private ContractStateEnum payerState; // done
  private ContractStateEnum payeeState; // done

  public ClassifiedListingEvent constructClassifiedListingEvent() {
    ArrayList<BaseTag> baseTags = new ArrayList<>();
    baseTags.add(GenericTag.create("payer_state", 52, String.valueOf(getPayerState())));
    baseTags.add(GenericTag.create("payee_state", 52, String.valueOf(getPayeeState())));
    return new ClassifiedListingEvent(
        new PublicKey(getNostrAppUserPubKey()),
        Kind.CLASSIFIED_LISTING,
        baseTags,
        "CLASSIFIED LISTING CONTENT",
        ClassifiedListing.builder(
                getText(),
                "CLASSIFIED LISTING SUMMARY",
                new PriceTag(getPayoutAmount(), "btc", "once"))
            .build());
  }

  public CalendarTimeBasedEvent constructCalendarTimeBasedEvent() {
    CalendarContent calendarContent = CalendarContent.builder(
            new IdentifierTag("UUID-NEEDS-COMPLETION-001"),
            getText(),
            getAgreedStartTime().getTime())
        .build();

//    TODO: needs design validation- give current/enclosing method is called by save(), should below always be done for:
//        a) appUser?
//    TODO: explicit add() used since contract *SHOULD* have creator's pubkey
    List<PubKeyTag> pubKeyTags = new ArrayList<>();

    pubKeyTags.add(
        new PubKeyTag(
            new PublicKey(
                getNostrAppUserPubKey()),
            "ws://localhost:5555",
            getCreatorRole().getCreatorRoleType()));

//    TODO: needs design validation- give current/enclosing method is called by save(), should below always be done for:
//        a) counterParty?
    Optional
        .ofNullable(
            getNostrCounterPartyPubKey())
        .ifPresent(counterPartyPubKey -> pubKeyTags.add(
            new PubKeyTag(new PublicKey(counterPartyPubKey))
        ));

    calendarContent.setParticipantPubKeys(pubKeyTags);

//    TODO: POC design, needs revisit
    ArrayList<BaseTag> baseTags = new ArrayList<>();
    Optional
        .ofNullable(
//            TODO: below time needs evolution
            getAgreedCompletionTime())
        .ifPresent(aDate ->
            baseTags.add(
                GenericTag.create(
                    "end",
                    52,
                    String.valueOf(aDate.getTime()))));

    CalendarTimeBasedEvent calendarTimeBasedEvent = new CalendarTimeBasedEvent(
        new PublicKey(getNostrAppUserPubKey()),
        baseTags,
        "CALENDAR-EVENT CONTENT",
        calendarContent
    );
    return calendarTimeBasedEvent;
  }
}
