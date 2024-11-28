package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import lombok.NonNull;

public interface ContractEntityServiceNostrDecoratorIF extends ContractEntityServiceIF {
  Contract getContractByIdAndPubKey(@NonNull Long id, @NonNull String pubKey);
}
