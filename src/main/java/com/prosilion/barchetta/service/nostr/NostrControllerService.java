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
  public Contract createContract(@NonNull ContractDto contractDto, @NonNull NostrUser user) throws IOException, NostrException, ExecutionException, InterruptedException {
    contractDto.setNostrAppUserPubKey(user.getPubkey());
    log.info("Set appUser userId [{}] to contract [{}]", contractDto.getAppUserId(), contractDto.getId());
    return save(contractDto);
  }

  @Override
  public Contract createContractCounterparty(@NonNull ContractDto contractDto, @NonNull NostrUser user) throws NostrException, IOException, ExecutionException, InterruptedException {
    User foundUser = findByUsername(user.getUsername());
    contractDto.setNostrCounterPartyPubKey(user.getPubkey());
    return save(contractDto);
  }

  @Override
  public ContractDto getContractDto(@NonNull Long id) {
    return controllerService.getContractDto(id);
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
  public Contract update(@NonNull ContractDto contractDto) throws NostrException, IOException, ExecutionException, InterruptedException {
    return save(contractDto);
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

  private Contract save(@NonNull ContractDto contractDto) throws IOException, ExecutionException, InterruptedException, NostrException {
    return contractEntityServiceNostrDecorator.save(contractDto.convertToEntity());
  }
}
