package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;

import java.util.List;

public interface ContractServiceIF {
  @Transactional
  Contract save(@NonNull Contract contract);
  Contract getContractById(@NonNull Long id);
  List<Contract> getContractsByAppUser(@NonNull AppUser appUser);
  List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser);
  List<Contract> getContractsByCoPartyId(@NonNull Long id);
  List<Contract> getAvailableOppositeRoleContractsByAppUserId(@NonNull Long id);
  List<Contract> getContractsByAppUserId(@NonNull Long id);
  List<Contract> getAll();
}
