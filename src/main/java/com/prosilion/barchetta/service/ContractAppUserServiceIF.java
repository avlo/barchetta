package com.prosilion.barchetta.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.barchetta.model.dto.ContractAppUserDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.ContractAppUser;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.presto.security.entity.AppUser;
import com.prosilion.presto.security.entity.AuthUserDetails;
import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface ContractAppUserServiceIF {
  Contract getContractByContractId(@NonNull Long id);
  ContractAppUser findUserByUserId(Long id);
  ContractAppUser findByUsername(@NonNull String username);
  Contract save(@NonNull Contract contract);
  List<Contract> getAll();
  @Transactional
  Contract create(@NonNull Contract contract, @NonNull Long userId) throws JsonProcessingException;
  ContractAppUserDto update(@NonNull ContractAppUserDto contractAppUserDto) throws InvocationTargetException, IllegalAccessException;
  List<Contract> getAllContractsFor(@NonNull AppUser appUser);
  List<Contract> getOpenContractsFor(@NonNull AppUser appUser);
  Contract constructContract(AppUser appUser);
  CreatorRoleEnum getRole(Contract contract, AuthUserDetails user);
}
