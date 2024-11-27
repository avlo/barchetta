package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.db.ContractEntityServiceIF;
import com.prosilion.barchetta.service.user.UserServiceIF;
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
  public ControllerService(ContractEntityServiceIF contractEntityService, UserServiceIF userService) {
    this.contractEntityService = contractEntityService;
    this.userService = userService;
  }

  @Override
  public Contract saveAsCreator(@NonNull ContractDto contractDto, @NonNull User user) throws IOException, NostrException, ExecutionException, InterruptedException {
    // TODO: check below contract doesn't already have existing different appuser ID or other/clean sol'n
    User foundUser = userService.findByUsername(user.getUsername());
    Contract contract = contractDto.convertToEntity();
    contract.setAppUserId(foundUser.getId());
    contract.setNostrAppUserPubKey(user.getPubkey());
    log.info("Set appUser userId [{}] to contract [{}]", contract.getAppUserId(), contract.getId());
    return save(contract);
  }

  @Override
  public Contract saveAsCounterParty(@NonNull Long contractId, @NonNull User user) throws NostrException, IOException, ExecutionException, InterruptedException {
    Contract contract = getContractByContractId(contractId);
    User foundUser = userService.findByUsername(user.getUsername());
    contract.setCounterPartyId(foundUser.getId());
    contract.setNostrCounterPartyPubKey(user.getPubkey());
    return save(contract);
  }

  @Override
  public ContractDto getContractDtoByContractId(@NonNull Long id) {
    return getContractByContractId(id).convertToDto();
  }

  @Override
  public Contract getContractByContractId(@NonNull Long id) {
    return contractEntityService.getContractById(id);
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
  public List<Contract> getAll() {
    return contractEntityService.getAll();
  }

  @Override
  public List<Contract> getAllContractsFor(@NonNull User appUser) {
    return Stream.concat(
            contractEntityService.getContractsByAppUser(appUser).stream(),
            contractEntityService.getContractsByCoParty(appUser).stream())
        .toList();
  }

  @Override
  public List<Contract> getOpenContractsFor(@NonNull User appUser) {
    return contractEntityService.getAvailableOppositeRoleContractsByAppUser(appUser);
  }

  @Override
  public ContractDto constructContractDto() {
    return new ContractDto();
  }
}
