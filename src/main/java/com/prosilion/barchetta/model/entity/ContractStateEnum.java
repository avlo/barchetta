package com.prosilion.barchetta.model.entity;

import lombok.Getter;

public enum ContractStateEnum {
  APPROVE("Approve"),
  VETO("Veto");

  @Getter private final String ContractStateType;

  ContractStateEnum(String ContractStateType) {
    this.ContractStateType = ContractStateType;
  }
}
