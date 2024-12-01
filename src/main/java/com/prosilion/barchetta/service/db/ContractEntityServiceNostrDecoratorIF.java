package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import lombok.NonNull;

import java.util.List;

public interface ContractEntityServiceNostrDecoratorIF extends ContractEntityServiceIF {
  Contract getContract(@NonNull Long contractId, @NonNull String pubKeySubscriptionId);
  List<Contract> getAllContracts(@NonNull String pubKeySubscriptionId);
}
