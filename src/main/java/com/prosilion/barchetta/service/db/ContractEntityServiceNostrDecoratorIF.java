package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import lombok.NonNull;

import java.util.List;

public interface ContractEntityServiceNostrDecoratorIF extends ContractEntityServiceIF {
  Contract getNostrContract(@NonNull Long contractId);
  List<Contract> getAllContractsBySubscriberId(@NonNull String pubKeySubscriptionId);
}
