package com.prosilion.barchetta.model.dto;

import com.prosilion.barchetta.model.entity.Contract;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;

@Data
@AllArgsConstructor
public class ContractDto {
  private PubKeyTag pubKeyTag;
  private PriceTag priceTag;
  private String classifiedListingEventJson;
  private String calendarTimeBasedEventJson;

  public Contract convertToEntity() {
    return new Contract(
        mapJsonToEvent(classifiedListingEventJson, ClassifiedListingEvent.class),
        mapJsonToEvent(calendarTimeBasedEventJson, CalendarTimeBasedEvent.class)
    );
  }

  private static <T extends GenericEvent> T mapJsonToEvent(String eventJson, Class<T> clazz) {
    return new GenericEventDecoder<>(clazz).decode(eventJson);
  }
}
