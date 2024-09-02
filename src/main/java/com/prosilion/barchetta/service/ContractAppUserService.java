package com.prosilion.barchetta.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.barchetta.model.dto.ContractAppUserDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.ContractAppUser;
import com.prosilion.barchetta.model.entity.ContractStateEnum;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.repository.ContractUserRepository;
import com.prosilion.presto.security.entity.AppUser;
import com.prosilion.presto.security.entity.AuthUserDetails;
import com.prosilion.presto.security.service.AuthUserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ContractAppUserService {
  private final ContractServiceIF contractServiceIF;
  private final ContractUserRepository contractUserRepository;
  private final AuthUserService authUserService;

  @Autowired
  public ContractAppUserService(ContractServiceIF contractServiceIF, ContractUserRepository contractUserRepository, AuthUserService authUserService) {
    this.contractServiceIF = contractServiceIF;
    this.contractUserRepository = contractUserRepository;
    this.authUserService = authUserService;
  }

  public Contract getContractByContractId(@NonNull Long id) {
    return contractServiceIF.getContractById(id);
  }

  public ContractAppUser findUserByUserId(Long id) {
    return contractUserRepository.findById(id).get();
  }

  public ContractAppUser findByUsername(@NonNull String username) {
    return findUserByUserId(authUserService.getAppuserAuthuser(username).getId());
  }

  public Contract save(@NonNull Contract contract) {
    return contractServiceIF.save(contract);
  }

  public List<Contract> getAll() {
    return contractServiceIF.getAll();
  }

  @Transactional
  public Contract create(@NonNull Contract contract, @NonNull Long userId) throws JsonProcessingException {
    log.info("Creating contract [{}], for user userId [{}]", contract.getText(), userId);
    // TODO: check below contract doesn't already have existing different appuser ID
    contract.setAppUserId(contractUserRepository.findById(userId).get().getId());
    log.info("Set appUser userId [{}] to contract [{}]", contract.getAppUserId(), contract.getId());
    return create(contract);
  }

  public ContractAppUserDto update(@NonNull ContractAppUserDto contractAppUserDto) throws InvocationTargetException, IllegalAccessException {
    log.info("CONTRACT USER - updating");
    ContractAppUser contractAppUser = contractAppUserDto.convertToContractAppUser();
    ContractAppUser retrievedUser = find(contractAppUser);
    log.info("Confirm retrieved existing contractAppUser [{}]", retrievedUser);
    ContractAppUser returnUser = contractUserRepository.save(contractAppUser);
    log.info("Updating contractAppUser [{}]", returnUser);
    return contractUserRepository.findById(contractAppUser.getId()).get().convertToDto();
  }

  public List<Contract> getAllContractsFor(@NonNull AppUser appUser) {
    List<Contract> contracts = contractServiceIF.getContractsByAppUserId(appUser.getId());
    contracts.addAll(contractServiceIF.getContractsByCoPartyId(appUser.getId()));
    return contracts;
  }

  public List<Contract> getOpenContractsFor(@NonNull AppUser appUser) {
    return contractServiceIF.getAvailableOppositeRoleContractsByAppUserId(appUser.getId());
  }

  public Contract constructContract(AppUser appUser) {
    return constructContract(appUser.getId());
  }

  public CreatorRoleEnum getRole(Contract contract, AuthUserDetails user) {
    return getRoleEnum(
        contract.getCreatorRole(),
        contract.getAppUserId(),
        findByUsername(user.getUsername()).getId());
  }

  private ContractAppUser find(@NonNull ContractAppUser contractAppUser) {
    return Objects.isNull(contractAppUser.getId()) ? contractAppUser : findUserByUserId(contractAppUser.getId());
  }

  private Contract create(@NonNull Contract contract) throws JsonProcessingException {
    log.info("Saving contract [{}], appUser ID [{}], role [{}]", contract.getText(), contract.getAppUserId(), contract.getCreatorRole());
    Contract savedContract = contractServiceIF.save(contract);
    log.info("Contract saved [{}], appUser ID [{}], role [{}]", savedContract.getText(), savedContract.getAppUserId(), savedContract.getCreatorRole());
    return savedContract;
  }

  private Contract constructContract(@NonNull Long id) {
    Contract contract = new Contract();
    contract.setAppUserId(id);
    contract.setPayerState(ContractStateEnum.APPROVE);
    contract.setPayeeState(ContractStateEnum.APPROVE);
    return contract;
  }

  private CreatorRoleEnum getRoleEnum(@NonNull CreatorRoleEnum role, @NonNull Long contractAppUserId, @NonNull Long appUserId) {
    if (contractAppUserId.equals(appUserId) && role.equals(CreatorRoleEnum.PAYER)) {
      return CreatorRoleEnum.PAYER;
    }
    if (contractAppUserId.equals(appUserId) && role.equals(CreatorRoleEnum.PAYEE)) {
      return CreatorRoleEnum.PAYEE;
    }
    if (!contractAppUserId.equals(appUserId) && role.equals(CreatorRoleEnum.PAYER)) {
      return CreatorRoleEnum.PAYEE;
    }
    return CreatorRoleEnum.PAYER;
  }
}
