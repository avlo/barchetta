package com.prosilion.barchetta.service.nostr;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.service.controller.ControllerServiceIF;
import com.prosilion.presto.nostr.entity.NostrUser;
import lombok.NonNull;
import nostr.util.NostrException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface NostrControllerServiceIF extends ControllerServiceIF {
  List<Contract> getAllNostrUserContracts(@NonNull NostrUser user);
  List<Contract> getOpenContracts(@NonNull NostrUser nostrUser);
  Contract getContract(@NonNull Long id) throws IOException, ExecutionException, InterruptedException;
  ContractDto getContractDto(@NonNull Long id);
  Contract update(@NonNull Contract contract) throws NostrException, IOException, ExecutionException, InterruptedException;
}
