package com.prosilion.barchetta.service.controller;

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
import java.util.UUID;
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
  public Contract createContract(@NonNull ContractDto contractDto, @NonNull NostrUser user) throws IOException, NostrException, ExecutionException, InterruptedException {
    // TODO: check below contract doesn't already have existing different appuser ID or other/clean sol'n
    contractDto.setAppUserId(
        userService.findByUsername(
                user.getUsername())
            .getId());
    log.info("contract id [{}], set AppUser to [{}] having AppUserId [{}]", contractDto.getId(), user.getUsername(), contractDto.getAppUserId());
    return save(contractDto);
  }

  @Override
  public Contract createContractCounterparty(@NonNull ContractDto contractDto, @NonNull NostrUser user) throws IOException, NostrException, ExecutionException, InterruptedException {
    contractDto.setCounterPartyId(
        userService.findByUsername(
                user.getUsername())
            .getId());
    log.info("contract id [{}], set CounterParty to [{}] having CounterPartyId [{}]", contractDto.getId(), user.getUsername(), contractDto.getCounterPartyId());
    return save(contractDto);
  }

  @Override
  public ContractDto getContractDto(@NonNull Long contractId) {
    return contractEntityService.getContract(contractId).convertToDto();
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
  public Contract update(@NonNull ContractDto contractDto) throws NostrException, IOException, ExecutionException, InterruptedException {
    return save(contractDto);
  }

  @Override
  public List<Contract> getAllContracts() {
    return contractEntityService.getAllContracts();
  }

  @Override
  public List<Contract> getAllUserContracts(@NonNull User appUser) {
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
    ContractDto contractDto = new ContractDto();
    String uuid = UUID.randomUUID().toString().concat(UUID.randomUUID().toString()).substring(0, 64);
    contractDto.setEventUuid(uuid);
    return contractDto;
  }

  private Contract save(@NonNull ContractDto contractDto) throws IOException, ExecutionException, InterruptedException, NostrException {
    return contractEntityService.save(contractDto.convertToEntity());
  }
}
