package com.prosilion.barchetta.model.entity;

import lombok.Getter;

public enum CreatorRoleEnum {
  PAYER("Payer"),
  PAYEE("Payee");

  @Getter private final String creatorRoleType;

  CreatorRoleEnum(String creatorRoleType) {
    this.creatorRoleType = creatorRoleType;
  }
}
