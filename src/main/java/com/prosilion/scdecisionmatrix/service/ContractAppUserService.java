package com.prosilion.scdecisionmatrix.service;

import com.prosilion.scdecisionmatrix.model.dto.ContractAppUserDto;
import com.prosilion.scdecisionmatrix.model.entity.Contract;
import com.prosilion.scdecisionmatrix.model.entity.ContractAppUser;
import com.prosilion.scdecisionmatrix.model.entity.ContractStateEnum;
import com.prosilion.scdecisionmatrix.repository.ContractUserRepository;
import edu.mayo.lpea.cad.cadence3.security.entity.AppUser;
import edu.mayo.lpea.cad.cadence3.security.service.AuthUserService;
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
public class ContractAppUserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContractAppUserService.class);
  private final ContractService contractService;
  private final ContractUserRepository contractUserRepository;
  private final AuthUserService authUserService;

  @Autowired
  public ContractAppUserService(ContractService contractService, ContractUserRepository contractUserRepository, AuthUserService authUserService) {
    this.contractService = contractService;
    this.contractUserRepository = contractUserRepository;
    this.authUserService = authUserService;
  }

  public ContractAppUser findById(Long id) {
    return contractUserRepository.findById(id).get();
  }

  public ContractAppUser findByUsername(@NonNull String username) {
    return findById(authUserService.getAppuserAuthuser(username).getId());
  }

  @Transactional
  public Contract save(@NonNull Contract contract, @NonNull Long id) {
    LOGGER.info("Creating contract [{}], for user id [{}]", contract.getText(), id);
    // TODO: check below contract doesn't already have existing different appuser ID
    contract.setAppUserId(contractUserRepository.findById(id).get().getId());
    LOGGER.info("Set appUser id [{}] to contract [{}]", contract.getAppUserId(), contract.getId());
    return save(contract);
  }

  public ContractAppUserDto update(@NonNull ContractAppUserDto contractAppUserDto) throws InvocationTargetException, IllegalAccessException {
    LOGGER.info("CONTRACT USER - updating");
    ContractAppUser contractAppUser = contractAppUserDto.convertToContractAppUser();
    ContractAppUser retrievedUser = find(contractAppUser);
    LOGGER.info("Confirm retrieved existing contractAppUser [{}]", retrievedUser);
    ContractAppUser returnUser = contractUserRepository.save(contractAppUser);
    LOGGER.info("Updating contractAppUser [{}]", returnUser);
    return contractUserRepository.findById(contractAppUser.getId()).get().convertToDto();
  }

  public List<Contract> getAllContractsFor(@NonNull AppUser appUser) {
    List<Contract> contracts = contractService.getContractsByAppUserId(appUser.getId());
    contracts.addAll(contractService.getContractsByCoPartyId(appUser.getId()));
    return contracts;
  }

  public List<Contract> getOpenContractsFor(@NonNull AppUser appUser) {
    return contractService.getAvailableOppositeRoleContractsByAppUserId(appUser.getId());
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

  private Contract save(@NonNull Contract contract) {
    LOGGER.info("Saving contract [{}], appUser ID [{}], role [{}]", contract.getText(), contract.getAppUserId(), contract.getCreatorRole());
    Contract savedContract = contractService.save(contract);
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
