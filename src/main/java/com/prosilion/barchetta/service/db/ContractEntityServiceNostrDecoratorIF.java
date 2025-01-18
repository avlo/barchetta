package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import lombok.NonNull;
import nostr.util.NostrException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface ContractEntityServiceNostrDecoratorIF extends ContractEntityServiceIF {
  Contract getContract(@NonNull Contract contract);
  Contract update(@NonNull Contract contract) throws NostrException, IOException, ExecutionException, InterruptedException;
}
