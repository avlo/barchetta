package com.prosilion.barchetta.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.barchetta.model.dto.ContractAppUserDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.ContractAppUser;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.presto.security.entity.AppUser;
import com.prosilion.presto.security.entity.AuthUserDetails;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Slf4j
public class ContractAppUserServiceNostrDecorator implements ContractAppUserServiceIF {
  private final ContractAppUserService contractAppUserService;

  public ContractAppUserServiceNostrDecorator(ContractAppUserService contractAppUserService) {
    this.contractAppUserService = contractAppUserService;
  }

  @Override
  public Contract getContractByContractId(@NonNull Long id) {
    return contractAppUserService.getContractByContractId(id);
  }

  @Override
  public ContractAppUser findUserByUserId(Long id) {
    return contractAppUserService.findUserByUserId(id);
  }

  public ContractAppUser findByUsername(@NonNull String username) {
    return contractAppUserService.findByUsername(username);
  }

  @Override
  public Contract save(@NonNull Contract contract) {
    return contractAppUserService.save(contract);
  }

  @Override
  public List<Contract> getAll() {
    return contractAppUserService.getAll();
  }

  @Transactional
  public Contract create(@NonNull Contract contract, @NonNull Long id) throws JsonProcessingException {
    log.info("Creating contract [{}], for nostr user id [{}]", contract.getText(), id);
    log.info("Set nostr appUser id [{}] to contract [{}]", contract.getAppUserId(), contract.getId());
    return contractAppUserService.create(contract, id);
  }

  public ContractAppUserDto update(@NonNull ContractAppUserDto contractAppUserDto) throws InvocationTargetException, IllegalAccessException {
    log.info("CONTRACT NOSTR USER - updating");
    return contractAppUserService.update(contractAppUserDto);
  }

  public List<Contract> getAllContractsFor(@NonNull AppUser appUser) {
    return contractAppUserService.getAllContractsFor(appUser);
  }

  public List<Contract> getOpenContractsFor(@NonNull AppUser appUser) {
    return contractAppUserService.getOpenContractsFor(appUser);
  }

  public Contract constructContract(AppUser appUser) {
    return contractAppUserService.constructContract(appUser);
  }

  @Override
  public CreatorRoleEnum getRole(Contract contract, AuthUserDetails user) {
    return contractAppUserService.getRole(contract, user);
  }
}
