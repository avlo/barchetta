package com.prosilion.barchetta.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.presto.security.entity.AppUser;
import com.prosilion.presto.security.entity.AuthUserDetails;
import lombok.NonNull;
import nostr.util.NostrException;

import java.io.IOException;
import java.util.List;

public interface ControllerServiceIF {
  Contract create(@NonNull Contract contract, @NonNull Long userId) throws IOException, NostrException;
  Contract getContractByContractId(@NonNull Long id) throws IOException;
  User findByUsername(@NonNull String username);
  CreatorRoleEnum getRole(Contract contract, AuthUserDetails user);
  Contract save(@NonNull Contract contract) throws NostrException, IOException;
  Contract saveAsCounterParty(@NonNull Contract contract, @NonNull User user) throws NostrException, IOException;
  List<Contract> getAll();
  List<Contract> getAllContractsFor(@NonNull AppUser appUser);
  List<Contract> getOpenContractsFor(@NonNull AppUser appUser);
  Contract constructContract(AppUser appUser);
}
