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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

@Service
public class ContractNostrAppUserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContractNostrAppUserService.class);
  private final ContractServiceIF contractServiceIf;

  @Autowired
  public ContractNostrAppUserService(ContractServiceIF contractServiceIf, ContractUserRepository contractUserRepository, AuthUserService authUserService) {
    this.contractServiceIf = contractServiceIf;
  }

  public ContractAppUser findById(Long id) {
    return null;// contractUserRepository.findById(id).get();
  }

  public ContractAppUser findByUsername(@NonNull String username) {
    return null; // findById(authUserService.getAppuserAuthuser(username).getId());
  }

  @Transactional
  public Contract create(@NonNull Contract contract, @NonNull Long id) throws JsonProcessingException {
    LOGGER.info("Creating contract [{}], for user id [{}]", contract.getText(), id);
    // TODO: check below contract doesn't already have existing different appuser ID
//    contract.setAppUserId(contractUserRepository.findById(id).get().getId());
    LOGGER.info("Set appUser id [{}] to contract [{}]", contract.getAppUserId(), contract.getId());
    return create(contract);
  }

  public ContractAppUserDto update(@NonNull ContractAppUserDto contractAppUserDto) throws InvocationTargetException, IllegalAccessException {
    LOGGER.info("CONTRACT USER - updating");
    ContractAppUser contractAppUser = contractAppUserDto.convertToContractAppUser();
    ContractAppUser retrievedUser = find(contractAppUser);
    LOGGER.info("Confirm retrieved existing contractAppUser [{}]", retrievedUser);
//    ContractAppUser returnUser = contractUserRepository.save(contractAppUser);
//    LOGGER.info("Updating contractAppUser [{}]", returnUser);
    return null; //contractUserRepository.findById(contractAppUser.getId()).get().convertToDto();
  }

  public List<Contract> getAllContractsFor(@NonNull AppUser appUser) {
    List<Contract> contracts = contractServiceIf.getContractsByAppUserId(appUser.getId());
    contracts.addAll(contractServiceIf.getContractsByCoPartyId(appUser.getId()));
    return contracts;
  }

  public List<Contract> getOpenContractsFor(@NonNull AppUser appUser) {
    return contractServiceIf.getAvailableOppositeRoleContractsByAppUserId(appUser.getId());
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
    LOGGER.info("Saving contract [{}], appUser ID [{}], role [{}]", contract.getText(), contract.getAppUserId(), contract.getCreatorRole());
    ;
    Contract savedContract = contractServiceIf.save(contract);
    LOGGER.info("Contract saved [{}], appUser ID [{}], role [{}]", savedContract.getText(), savedContract.getAppUserId(), savedContract.getCreatorRole());
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
