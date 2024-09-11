package com.prosilion.barchetta.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.presto.security.entity.AppUser;
import com.prosilion.presto.security.entity.AuthUserDetails;
import lombok.NonNull;

import java.util.List;

public interface ControllerServiceIF {
  Contract create(@NonNull Contract contract, @NonNull Long userId) throws JsonProcessingException;
  Contract getContractByContractId(@NonNull Long id);
  User findByUsername(@NonNull String username);
  CreatorRoleEnum getRole(Contract contract, AuthUserDetails user);
  Contract save(@NonNull Contract contract);
  List<Contract> getAll();
  List<Contract> getAllContractsFor(@NonNull AppUser appUser);
  List<Contract> getOpenContractsFor(@NonNull AppUser appUser);
  Contract constructContract(AppUser appUser);
}
