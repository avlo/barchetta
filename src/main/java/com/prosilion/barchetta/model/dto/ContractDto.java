package com.prosilion.barchetta.model.dto;

import com.prosilion.barchetta.model.entity.Contract;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.message.EventMessage;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;

import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class ContractDto {
  private PubKeyTag pubKeyTag;
  private PriceTag priceTag;
  private String classifiedListingEventDto;
  private String calendarTimeBasedEventDto;

  public Contract convertToEntity() {
    ClassifiedListingEvent classifiedListingEvent = mapJsonToEvent(List.of(classifiedListingEventDto), ClassifiedListingEvent.class);
    CalendarTimeBasedEvent calendarTimeBasedEvent = mapJsonToEvent(List.of(calendarTimeBasedEventDto), CalendarTimeBasedEvent.class);
    return new Contract(
        classifiedListingEvent,
        calendarTimeBasedEvent
    );
  }

  private <T extends GenericEvent> T mapJsonToEvent(List<String> reqResponse, Class<T> clazz) {
    Optional<T> first = reqResponse
        .stream()
        .map(baseMessage -> new BaseMessageDecoder<EventMessage>().decode(baseMessage))
        .map(eventMessage -> ((GenericEvent) eventMessage.getEvent()))
        .map(event -> new BaseEventEncoder<>(event).encode())
        .map(encode -> new GenericEventDecoder<>(clazz).decode(encode))
        .findFirst();
    T t = first.get();
    return t;
  }
}
