package com.prosilion.barchetta.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.impl.CalendarContent;
import nostr.event.impl.CalendarTimeBasedEvent;
import nostr.event.tag.IdentifierTag;
import nostr.util.NostrUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarTimeBasedEventDto {
  private String id;
  private String pubkey;
  private Integer kind;
  private Date created_at;
  private String content;
  private List<BaseTag> tags;
  private String sig;

  CalendarTimeBasedEvent convertToEntity() {
    IdentifierTag identifierTag = tags.stream()
        .filter(IdentifierTag.class::isInstance)
        .map(IdentifierTag.class::cast)
        .findFirst().orElseThrow();

    CalendarContent calendarContent = CalendarContent.builder(identifierTag, content, created_at.getTime()).build();
    calendarContent.setId(id);

    CalendarTimeBasedEvent calendarTimeBasedEvent = new CalendarTimeBasedEvent(
        new PublicKey(pubkey),
//        TODO: below needs filling
        new ArrayList<BaseTag>(),
        content,
        calendarContent
    );

    Signature signature = new Signature();
    signature.setRawData(NostrUtil.hexToBytes(sig));
    signature.setPubKey(new PublicKey(pubkey));
    calendarTimeBasedEvent.setSignature(signature);

    return calendarTimeBasedEvent;
  }
}
