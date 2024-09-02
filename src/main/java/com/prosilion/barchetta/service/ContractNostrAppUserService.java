package com.prosilion.barchetta.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.barchetta.model.dto.ContractAppUserDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.ContractAppUser;
import com.prosilion.barchetta.model.entity.ContractStateEnum;
import com.prosilion.barchetta.repository.ContractUserRepository;
import com.prosilion.presto.security.entity.AppUser;
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
public class ContractNostrAppUserService {
  private final ContractServiceIF contractServiceIF;

  @Autowired
  public ContractNostrAppUserService(ContractServiceIF contractServiceIF, ContractUserRepository contractUserRepository, AuthUserService authUserService) {
    this.contractServiceIF = contractServiceIF;
  }

  public ContractAppUser findById(Long id) {
    return null;// contractUserRepository.findById(id).get();
  }

  public ContractAppUser findByUsername(@NonNull String username) {
    return null; // findById(authUserService.getAppuserAuthuser(username).getId());
  }

  @Transactional
  public Contract create(@NonNull Contract contract, @NonNull Long id) throws JsonProcessingException {
    log.info("Creating contract [{}], for user id [{}]", contract.getText(), id);
    // TODO: check below contract doesn't already have existing different appuser ID
//    contract.setAppUserId(contractUserRepository.findById(id).get().getId());
    log.info("Set appUser id [{}] to contract [{}]", contract.getAppUserId(), contract.getId());
    return create(contract);
  }

  public ContractAppUserDto update(@NonNull ContractAppUserDto contractAppUserDto) throws InvocationTargetException, IllegalAccessException {
    log.info("CONTRACT USER - updating");
    ContractAppUser contractAppUser = contractAppUserDto.convertToContractAppUser();
    ContractAppUser retrievedUser = find(contractAppUser);
    log.info("Confirm retrieved existing contractAppUser [{}]", retrievedUser);
//    ContractAppUser returnUser = contractUserRepository.save(contractAppUser);
//    log.info("Updating contractAppUser [{}]", returnUser);
    return null; //contractUserRepository.findById(contractAppUser.getId()).get().convertToDto();
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

  ////////////////////////
  //  PRIVATE METHODS
  ////////////////////////

  private ContractAppUser find(@NonNull ContractAppUser contractAppUser) {
    return Objects.isNull(contractAppUser.getId()) ? contractAppUser : findById(contractAppUser.getId());
  }

  private Contract create(@NonNull Contract contract) throws JsonProcessingException {
    log.info("Saving contract [{}], appUser ID [{}], role [{}]", contract.getText(), contract.getAppUserId(), contract.getCreatorRole());
    ;
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
}
