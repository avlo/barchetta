package com.prosilion.scdecisionmatrix.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Contract {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nostrAppUserId;
  private String nostrCounterPartyId;

  private String text;
  private Long payerStake, payeeStake, payoutAmount;
  private Boolean completed;
  private Date agreedCompletionTime;

  private CreatorRoleEnum creatorRole;
  private ContractStateEnum payerState;
  private ContractStateEnum payeeState;

  private Long appUserId;
  private Long counterPartyId;
}
