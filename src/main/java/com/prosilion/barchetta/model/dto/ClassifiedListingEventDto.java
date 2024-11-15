package com.prosilion.barchetta.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nostr.base.PublicKey;
import nostr.base.Signature;
import nostr.event.BaseTag;
import nostr.event.Kind;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.tag.PriceTag;
import nostr.util.NostrUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassifiedListingEventDto {
  private String id;
  private String pubkey;
  private Integer kind;
  private Date created_at;
  private String content;
  private List<BaseTag> tags;
  private String sig;

  ClassifiedListingEvent convertToEntity() {
    PriceTag priceTag = tags.stream()
        .filter(PriceTag.class::isInstance)
        .map(PriceTag.class::cast)
        .findFirst().orElseThrow();

    ClassifiedListing classifiedListing = ClassifiedListing.builder(content, content, priceTag).build();
    classifiedListing.setId(id);
    classifiedListing.setPublishedAt(created_at.getTime());

    ClassifiedListingEvent classifiedListingEvent = new ClassifiedListingEvent(
        new PublicKey(pubkey),
        Kind.valueOf(kind),
//        TODO: below needs filling
        new ArrayList<BaseTag>(),
        content,
        classifiedListing
    );

    Signature signature = new Signature();
    signature.setRawData(NostrUtil.hexToBytes(sig));
    signature.setPubKey(new PublicKey(pubkey));
    classifiedListingEvent.setSignature(signature);

    return classifiedListingEvent;
  }
}
