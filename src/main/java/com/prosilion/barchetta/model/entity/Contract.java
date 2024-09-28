package com.prosilion.barchetta.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Contract {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long appUserId;
  private Long counterPartyId;

  private String nostrClassifiedListingEventId; // done
  private String nostrCalendarTimeBasedEventId;

  private String nostrAppUserPubKey;  // done
  private String nostrCounterPartyPubKey; // done, needs correctness confirmation

  private String text; // done
  private BigDecimal payerStake;
  private BigDecimal payeeStake;
  private BigDecimal payoutAmount; // done

  private Boolean completed;
  private Date agreedStartTime;
  private Date agreedCompletionTime; // done

  private CreatorRoleEnum creatorRole; // done
  private ContractStateEnum payerState; // done
  private ContractStateEnum payeeState; // done
}
