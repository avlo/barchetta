package com.prosilion.barchetta.model.dto;

import com.prosilion.barchetta.model.entity.Contract;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContractDto {
  private ClassifiedListingEventDto classifiedListingEventDto;
  private CalendarTimeBasedEventDto calendarTimeBasedEventDto;

  public Contract convertToEntity() {
    return new Contract(
        classifiedListingEventDto.convertToEntity(),
        calendarTimeBasedEventDto.convertToEntity()
    );
  }
}
