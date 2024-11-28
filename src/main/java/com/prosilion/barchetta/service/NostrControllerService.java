package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.db.ContractEntityServiceNostrDecoratorIF;
import lombok.NonNull;
import nostr.util.NostrException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
//  TODO: ContractDto needs NostrContractDto variant w/ cleEvent & ctbEvent refactored out of
//      ContractDto and into NostrContractDto
  public ContractDto constructContractDto() {
    return controllerService.constructContractDto();
  }

  @Override
  public Contract saveAsCreator(@NonNull ContractDto contract, @NonNull User user) throws IOException, NostrException, ExecutionException, InterruptedException {
    return controllerService.saveAsCreator(contract, user);
  }

  @Override
  public Contract getContractByContractId(@NonNull Long id) throws IOException {
    System.out.println("000000000000000");
    System.out.println("000000000000000");
    System.out.println("this should never get called in nostr context");
    System.out.println("double check such then uncomment below throw exception");
    System.out.println("000000000000000");
    System.out.println("000000000000000");
//    throw new IOException("NostrControllerService.getContractByContractId(@NonNull Long id) has been called but should be NostrControllerService.getContractDtoByContractIdAndPubkey(@NonNull Long id, @NotNull User user) instead");
    return controllerService.getContractByContractId(id);
  }

  @Override
  public ContractDto getContractDtoByContractIdAndPubkey(@NonNull Long id, @NotNull User user) {
    return contractEntityServiceNostrDecorator.getContractByIdAndPubKey(id, user.getPubkey()).convertToDto();
  }

  @Override
  public ContractDto getContractDtoByContractId(@NonNull Long id) {
    System.out.println("111111111111111");
    System.out.println("111111111111111");
    System.out.println("this should never get called in nostr context");
    System.out.println("double check such then uncomment below throw exception");
    System.out.println("111111111111111");
    System.out.println("111111111111111");
//    throw new IOException("NostrControllerService.getContractByContractId(@NonNull Long id) has been called but should be NostrControllerService.getContractDtoByContractIdAndPubkey(@NonNull Long id, @NotNull User user) instead");
    return controllerService.getContractDtoByContractId(id);
  }

  @Override
  public User findByUsername(@NonNull String username) {
    return controllerService.findByUsername(username);
  }

  @Override
  public CreatorRoleEnum getRole(Contract contract, User user) {
    return controllerService.getRole(contract, user);
  }

  @Override
  public Contract saveDto(@NonNull ContractDto contractDto) throws NostrException, IOException, ExecutionException, InterruptedException {
    return controllerService.saveDto(contractDto);
  }

  @Override
  public Contract save(@NonNull Contract contract) throws NostrException, IOException, ExecutionException, InterruptedException {
    return controllerService.save(contract);
  }

  @Override
  public Contract saveAsCounterParty(@NonNull Long contractId, @NonNull User user) throws NostrException, IOException, ExecutionException, InterruptedException {
    return controllerService.saveAsCounterParty(contractId, user);
  }

  @Override
  public List<Contract> getAll() {
    return controllerService.getAll();
  }

  @Override
  public List<Contract> getAllContractsFor(@NonNull User user) {
    return controllerService.getAllContractsFor(user);
  }

  @Override
  public List<Contract> getOpenContractsFor(@NonNull User user) {
    return controllerService.getOpenContractsFor(user);
  }
}
