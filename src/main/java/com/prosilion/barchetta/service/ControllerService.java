package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.db.ContractEntityServiceIF;
import com.prosilion.barchetta.service.user.UserServiceIF;
import com.prosilion.presto.nostr.entity.NostrUser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Slf4j
@Service
public class ControllerService implements ControllerServiceIF {
  ContractEntityServiceIF contractEntityService;
  UserServiceIF userService;

  @Autowired
  public ControllerService(
      @NonNull ContractEntityServiceIF contractEntityService,
      @NonNull UserServiceIF userService) {
    this.contractEntityService = contractEntityService;
    this.userService = userService;
  }

  @Override
  public Contract saveAsCreator(@NonNull ContractDto contractDto, @NonNull NostrUser user) throws IOException, NostrException, ExecutionException, InterruptedException {
    // TODO: check below contract doesn't already have existing different appuser ID or other/clean sol'n
    User foundUser = userService.findByUsername(user.getUsername());
    Contract contract = contractDto.convertToEntity();
    contract.setAppUserId(foundUser.getId());
//    TODO: below line should be refactored into NostrControllerService
    contract.setNostrAppUserPubKey(user.getPubkey());
    log.info("Set appUser userId [{}] to contract [{}]", contract.getAppUserId(), contract.getId());
    return save(contract);
  }

  @Override
  public Contract saveAsCounterParty(@NonNull Long contractId, @NonNull NostrUser user) throws NostrException, IOException, ExecutionException, InterruptedException {
    Contract contract = getContract(contractId);
    User foundUser = userService.findByUsername(user.getUsername());
    contract.setCounterPartyId(foundUser.getId());
//    TODO: below line should be refactored into NostrControllerService
    contract.setNostrCounterPartyPubKey(user.getPubkey());
    return save(contract);
  }

  @Override
  public ContractDto getContractDto(@NonNull Long contractId) {
    return getContract(contractId).convertToDto();
  }

  @Override
  public Contract getContract(@NonNull Long contractId) {
    return contractEntityService.getContract(contractId);
  }

  @Override
  public User findByUsername(@NonNull String username) {
    return userService.findByUsername(username);
  }

  @Override
  public CreatorRoleEnum getRole(Contract contract, User user) {
    return userService.getRole(contract, user);
  }

  @Override
  public Contract saveDto(@NonNull ContractDto contractDto) throws NostrException, IOException, ExecutionException, InterruptedException {
    return save(contractDto.convertToEntity());
  }

  @Override
  public Contract save(@NonNull Contract contract) throws NostrException, IOException, ExecutionException, InterruptedException {
    return contractEntityService.save(contract);
  }

  @Override
  public List<Contract> getAllContracts() {
    return contractEntityService.getAllContracts();
  }

  @Override
  public List<Contract> getAllContracts(@NonNull User appUser) {
    return Stream.concat(
            contractEntityService.getContractsByAppUser(appUser).stream(),
            contractEntityService.getContractsByCoParty(appUser).stream())
        .toList();
  }

  @Override
  public List<Contract> getOpenContracts(@NonNull User appUser) {
    return contractEntityService.getAvailableOppositeRoleContractsByAppUser(appUser);
  }

  @Override
  public ContractDto constructContractDto() {
    return new ContractDto();
  }
}
