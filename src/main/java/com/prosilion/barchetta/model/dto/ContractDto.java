package com.prosilion.barchetta.model.dto;

import com.prosilion.barchetta.model.entity.Contract;
import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.event.tag.PriceTag;
import nostr.event.tag.PubKeyTag;

@Data
@AllArgsConstructor
public class ContractDto {
  private PubKeyTag pubKeyTag;
  private PriceTag priceTag;
  private ClassifiedListingEventDto classifiedListingEventDto;
  private CalendarTimeBasedEventDto calendarTimeBasedEventDto;

  public Contract convertToEntity() {
    return new Contract(
        classifiedListingEventDto.convertToEntity(),
        calendarTimeBasedEventDto.convertToEntity()
    );
  }
}
