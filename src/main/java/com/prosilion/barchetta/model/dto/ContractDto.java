package com.prosilion.barchetta.model.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.ContractStateEnum;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.GenericEventDecoder;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractDto {
  private Long id;
  private Long appUserId;
  private Long counterPartyId;
  private String eventUuid;
  private CreatorRoleEnum creatorRole;
  private BigDecimal price;
  private BigDecimal payerStake;
  private BigDecimal payeeStake;
  private boolean completed;
  private ContractStateEnum payerState;
  private ContractStateEnum payeeState;
  private String nostrAppUserPubKey;
  private String nostrCounterPartyPubKey;
  private String classifiedListingEventJson;
  private String calendarTimeBasedEventJson;
  private String text;

  public Contract convertToEntity() throws JsonProcessingException {
    return new Contract(
//        TODO: below two lines need refactor into a NostrContractDto variant
        mapJsonToEvent(classifiedListingEventJson, ClassifiedListingEvent.class),
        mapJsonToEvent(calendarTimeBasedEventJson, CalendarTimeBasedEvent.class),
        eventUuid
    );
  }

  public static <T extends GenericEvent> T mapJsonToEvent(String eventJson, Class<T> clazz) throws JsonProcessingException {
    return new GenericEventDecoder<>(clazz).decode(eventJson);
  }
}
