package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import nostr.util.NostrException;

import java.io.IOException;
import java.util.List;

public interface ContractEntityServiceIF {
  @Transactional
  Contract save(@NonNull Contract contract) throws IOException, NostrException;
  Contract getContractById(@NonNull Long id) throws IOException;
  List<Contract> getContractsByAppUser(@NonNull AppUser appUser);
  List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser);
  List<Contract> getContractsByCoPartyId(@NonNull Long id);
  List<Contract> getAvailableOppositeRoleContractsByAppUserId(@NonNull Long id);
  List<Contract> getContractsByAppUserId(@NonNull Long id);
  List<Contract> getAll();
}
