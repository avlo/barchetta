package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.db.ContractEntityServiceNostrDecoratorIF;
import com.prosilion.presto.nostr.entity.NostrUser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrException;
import org.jetbrains.annotations.NotNull;
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
    return save(contract);
  }

  @Override
  public Contract saveAsCounterParty(@NonNull Long contractId, @NonNull NostrUser user) throws NostrException, IOException, ExecutionException, InterruptedException {
    Contract contract = getContract(contractId, user);
    User foundUser = findByUsername(user.getUsername());
    contract.setCounterPartyId(foundUser.getId());
    contract.setNostrCounterPartyPubKey(user.getPubkey());
    return save(contract);
  }

  @Override
  public ContractDto getContractDto(@NonNull Long id, @NotNull NostrUser user) {
    return getContract(id, user).convertToDto();
  }

  @Override
  public Contract getContract(@NonNull Long id, @NotNull NostrUser user) {
    return contractEntityServiceNostrDecorator.getNostrContract(id);
  }

  @Override
  public Contract getContract(@NonNull Long id) throws IOException {
    System.out.println("000000000000000");
    System.out.println("000000000000000");
    System.out.println("this method should never get called in nostr context since it only requires id");
    System.out.println("confirm as such then uncomment below to throw exception");
    System.out.println("000000000000000");
    System.out.println("000000000000000");
    throw new IOException("NostrControllerService.getContract(@NonNull Long id) has been erroneously called.   NostrControllerService.getContract(@NonNull Long id, @NotNull User user) should be called instead");
  }

  @Override
  public ContractDto getContractDto(@NonNull Long id) throws IOException {
    System.out.println("111111111111111");
    System.out.println("111111111111111");
    System.out.println("this method should never get called in nostr context since it only requires id");
    System.out.println("confirm as such then uncomment below to throw exception");
    System.out.println("111111111111111");
    System.out.println("111111111111111");
    throw new IOException("NostrControllerService.getContractDto(@NonNull Long id) has been erroneously called.   NostrControllerService.getContractDto(@NonNull Long id, @NotNull User user) should be called instead");
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
    return save(contractDto.convertToEntity());
  }

  @Override
  public Contract save(@NonNull Contract contract) throws NostrException, IOException, ExecutionException, InterruptedException {
    return contractEntityServiceNostrDecorator.save(contract);
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
