package com.prosilion.barchetta.model.entity;

import lombok.Getter;

@Getter
public enum CreatorRoleEnum {
  PAYER("payer"),
  PAYEE("payee");

  private final String creatorRoleType;


  CreatorRoleEnum(String creatorRoleType) {
    this.creatorRoleType = creatorRoleType;
  }
}
