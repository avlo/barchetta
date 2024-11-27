package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.User;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import nostr.util.NostrException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ContractEntityServiceIF {
  @Transactional
  Contract save(@NonNull Contract contract) throws IOException, NostrException, ExecutionException, InterruptedException;
  Contract getContractById(@NonNull Long id);
  List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull User appUser);
  List<Contract> getContractsByAppUser(@NonNull User appUser);
  List<Contract> getContractsByCoParty(@NonNull User coParty);
  List<Contract> getAll();
}
