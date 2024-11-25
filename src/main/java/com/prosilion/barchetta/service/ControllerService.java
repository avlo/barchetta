package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.db.ContractEntityServiceIF;
import com.prosilion.barchetta.service.user.UserServiceIF;
import com.prosilion.presto.nostr.entity.NostrUser;
import com.prosilion.presto.security.entity.AppUser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
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
  public Contract createContract(@NonNull ContractDto contractDto, @NonNull NostrUser user) throws IOException, NostrException {
    // TODO: check below contract doesn't already have existing different appuser ID
    User foundUser = userService.findByUsername(user.getUsername());
    Contract contract = contractDto.convertToEntity();
    contract.setAppUserId(foundUser.getId());
    contract.setNostrAppUserPubKey(user.getPubkey());
    log.info("Set appUser userId [{}] to contract [{}]", contract.getAppUserId(), contract.getId());
    return create(contract);
  }

  @Override
  public Contract saveAsCounterParty(@NonNull Long contractId, @NonNull NostrUser user) throws NostrException, IOException {
    User foundUser = userService.findByUsername(user.getUsername());
    Contract contract = getContractByContractId(contractId);
    contract.setNostrCounterPartyPubKey(foundUser.getPubkey());
    return save(contract);
  }

  @Override
  public Contract getContractByContractId(@NonNull Long id) throws IOException {
    return contractEntityService.getContractById(id);
  }

  @Override
  public ContractDto getContractDtoByContractId(@NonNull Long id) throws IOException {
    return contractEntityService.getContractById(id).convertToDto();
  }

  @Override
  public User findByUsername(@NonNull String username) {
    return userService.findByUsername(username);
  }

  @Override
  public CreatorRoleEnum getRole(Contract contract, User user) {
    return userService.getRole(contract, user);
  }

  private Contract create(@NonNull Contract contract) throws IOException, NostrException {
    log.info("Saving contract [{}], appUser ID [{}], role [{}]", contract.getText(), contract.getAppUserId(), contract.getCreatorRole());
    Contract savedContract = save(contract);
    log.info("Contract saved [{}], appUser ID [{}], role [{}]", savedContract.getText(), savedContract.getAppUserId(), savedContract.getCreatorRole());
    return savedContract;
  }

  @Override
  public Contract saveDto(@NonNull ContractDto contractDto) throws NostrException, IOException {
    return contractEntityService.save(contractDto.convertToEntity());
  }

  @Override
  public Contract save(@NonNull Contract contract) throws NostrException, IOException {
    return contractEntityService.save(contract);
  }

  @Override
  public List<Contract> getAll() {
    return contractEntityService.getAll();
  }

  @Override
  public List<Contract> getAllContractsFor(@NonNull AppUser appUser) {
    return Stream.concat(
            contractEntityService.getContractsByAppUserId(appUser.getId()).stream(),
            contractEntityService.getContractsByCoPartyId(appUser.getId()).stream())
        .toList();
  }

  @Override
  public List<Contract> getOpenContractsFor(@NonNull AppUser appUser) {
    return contractEntityService.getAvailableOppositeRoleContractsByAppUserId(appUser.getId());
  }

  @Override
  public ContractDto constructContractDto(AppUser appUser) {
    return new ContractDto();
  }
}
