package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.presto.security.entity.AppUser;
import lombok.NonNull;
import nostr.util.NostrException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ContractEntityServiceIF {
  Contract getContract(@NonNull Long id);
  Contract save(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException, NostrException;

  List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser);
  List<Contract> getContractsByAppUser(@NonNull AppUser appUser);
  List<Contract> getContractsByCoParty(@NonNull AppUser coParty);
  List<Contract> getAllContracts();
}
