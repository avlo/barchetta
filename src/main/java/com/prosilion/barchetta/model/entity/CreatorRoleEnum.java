package com.prosilion.barchetta.model.entity;

import lombok.Getter;
import lombok.NonNull;

@Getter
public enum CreatorRoleEnum {
  PAYER("payer"),
  PAYEE("payee");

  private final String creatorRoleType;

  CreatorRoleEnum(@NonNull String roleType) {
    this.creatorRoleType = roleType;
  }

  public static CreatorRoleEnum getOppositeRole(@NonNull CreatorRoleEnum roleType) {
    return roleType.equals(PAYEE) ? PAYER : PAYEE;
  }
}
