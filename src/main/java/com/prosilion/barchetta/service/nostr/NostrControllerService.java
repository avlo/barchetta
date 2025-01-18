package com.prosilion.barchetta.service.nostr;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.controller.ControllerServiceIF;
import com.prosilion.barchetta.service.db.ContractEntityServiceNostrDecoratorIF;
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
public class NostrControllerService implements NostrControllerServiceIF {
  private final ContractEntityServiceNostrDecoratorIF contractEntityServiceNostrDecorator;
  private final ControllerServiceIF controllerService;

  @Autowired
  public NostrControllerService(
      @NonNull ContractEntityServiceNostrDecoratorIF contractEntityServiceNostrDecorator,
      @NonNull ControllerServiceIF controllerService) {
    this.contractEntityServiceNostrDecorator = contractEntityServiceNostrDecorator;
    this.controllerService = controllerService;
  }

  @Override
  public Contract saveAsCreator(@NonNull ContractDto contractDto, @NonNull NostrUser user) throws IOException, NostrException, ExecutionException, InterruptedException {
    User foundUser = findByUsername(user.getUsername());
    Contract contract = contractDto.convertToEntity();
    contract.setAppUserId(foundUser.getId());
    contract.setNostrAppUserPubKey(user.getPubkey());
    log.info("Set appUser userId [{}] to contract [{}]", contract.getAppUserId(), contract.getId());
    return create(contract);
  }

  @Override
  public Contract saveAsCounterParty(@NonNull ContractDto contractDto, @NonNull NostrUser user) throws NostrException, IOException, ExecutionException, InterruptedException {
    User foundUser = findByUsername(user.getUsername());
    Contract contract = contractDto.convertToEntity();
    contract.setCounterPartyId(foundUser.getId());
    contract.setNostrCounterPartyPubKey(user.getPubkey());
    return create(contract);
  }

  @Override
  public Contract saveAsCounterParty(@NonNull Long contractId, @NonNull NostrUser user) throws NostrException, IOException, ExecutionException, InterruptedException {
    Contract contract = getContract(contractId);
    User foundUser = findByUsername(user.getUsername());
    contract.setCounterPartyId(foundUser.getId());
    contract.setNostrCounterPartyPubKey(user.getPubkey());
    return create(contract);
  }

  @Override
  public ContractDto getContractDto(@NonNull Long id) {
    return getContract(id).convertToDto();
  }

  @Override
  public Contract getContract(@NonNull Long id) {
    return contractEntityServiceNostrDecorator.getContract(id);
  }

  @Override
  public User findByUsername(@NonNull String username) {
    return controllerService.findByUsername(username);
  }

  @Override
  public CreatorRoleEnum getRole(@NonNull Contract contract, @NonNull User user) {
    return controllerService.getRole(contract, user);
  }

  @Override
  public Contract saveDto(@NonNull ContractDto contractDto) throws NostrException, IOException, ExecutionException, InterruptedException {
    return create(contractDto.convertToEntity());
  }

  @Override
  public Contract create(@NonNull Contract contract) throws NostrException, IOException, ExecutionException, InterruptedException {
    return contractEntityServiceNostrDecorator.create(contract);
  }

  @Override
  public Contract update(@NonNull Contract contract) throws NostrException, IOException, ExecutionException, InterruptedException {
    return contractEntityServiceNostrDecorator.create(contract);
  }

  @Override
  public List<Contract> getAllContracts() {
    return contractEntityServiceNostrDecorator.getAllContracts();
  }

  @Override
  public List<Contract> getAllNostrUserContracts(@NonNull NostrUser nostrUser) {
    return getAllUserContracts(findByUsername(nostrUser.getUsername()));
  }

  @Override
  public List<Contract> getAllUserContracts(@NonNull User user) {
    return Stream.concat(
            contractEntityServiceNostrDecorator.getContractsByAppUser(user).stream(),
            contractEntityServiceNostrDecorator.getContractsByCoParty(user).stream())
        .toList();
  }

  @Override
  public List<Contract> getOpenContracts(@NonNull NostrUser appUser) {
    return getOpenContracts(findByUsername(appUser.getUsername()));
  }

  @Override
  public List<Contract> getOpenContracts(@NonNull User user) {
    return contractEntityServiceNostrDecorator.getAvailableOppositeRoleContractsByAppUser(user);
  }

  @Override
//  TODO: ContractDto needs NostrContractDto variant w/ cleEvent & ctbEvent refactored out of
//      ContractDto and into NostrContractDto
  public ContractDto constructContractDto() {
    return controllerService.constructContractDto();
  }
}
