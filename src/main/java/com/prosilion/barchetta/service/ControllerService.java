package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.ContractStateEnum;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.service.db.ContractEntityServiceIF;
import com.prosilion.barchetta.service.user.UserServiceIF;
import com.prosilion.presto.security.entity.AppUser;
import com.prosilion.presto.security.entity.AuthUserDetails;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class ControllerService implements ControllerServiceIF {

  String ALICE_PUB_KEY = "111df01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";
  String BOB_PUB_KEY = "222df01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f";

  ContractEntityServiceIF contractEntityService;
  UserServiceIF userService;

  @Autowired
  public ControllerService(ContractEntityServiceIF contractEntityService, UserServiceIF userService) {
    this.contractEntityService = contractEntityService;
    this.userService = userService;
  }

  @Override
  public Contract create(@NonNull Contract contract, @NonNull String userName) throws IOException, NostrException {
    log.info("Creating contract [{}], for userName [{}]", contract.getText(), userName);
    return create(contract, findByUsername(userName).getId());
  }

  @Override
  public Contract create(@NonNull Contract contract, @NonNull Long userId) throws IOException, NostrException {
    log.info("Creating contract [{}], for user userId [{}]", contract.getText(), userId);
    // TODO: check below contract doesn't already have existing different appuser ID
    User user = userService.findByUserId(userId);
    contract.setAppUserId(user.getId());
    setAliceBobPubKey(user, contract); // TODO: remove this call when pubKey work is underway
    log.info("Set appUser userId [{}] to contract [{}]", contract.getAppUserId(), contract.getId());
    return create(contract);
  }

  // TODO: below method when pubKey work is underway
  private void setAliceBobPubKey(User user, Contract contract) {
    if (user.getId().equals(1L)) {
      contract.setNostrAppUserPubKey(ALICE_PUB_KEY);
      user.setNostrPubKey(ALICE_PUB_KEY);
//      userService.update(user.convertToDto());
      return;
    }

    contract.setNostrCounterPartyPubKey(BOB_PUB_KEY);
    user.setNostrPubKey(BOB_PUB_KEY);
//    userService.update(user.convertToDto());
  }

  // TODO: remove this method when pubKey work is underway
  @Override
  public Contract saveAsCounterParty(@NonNull Contract contract, @NonNull User user) throws NostrException, IOException {
    User byUserId = userService.findByUserId(user.getId());
    setAliceBobPubKey(byUserId, contract);
    return save(contract);
  }

  @Override
  public Contract getContractByContractId(@NonNull Long id) throws IOException {
    return contractEntityService.getContractById(id);
  }

  @Override
  public User findByUsername(@NonNull String username) {
    return userService.findByUsername(username);
  }

  @Override
  public CreatorRoleEnum getRole(Contract contract, AuthUserDetails user) {
    return userService.getRole(contract, user);
  }

  private Contract create(@NonNull Contract contract) throws IOException, NostrException {
    log.info("Saving contract [{}], appUser ID [{}], role [{}]", contract.getText(), contract.getAppUserId(), contract.getCreatorRole());
    Contract savedContract = save(contract);
    log.info("Contract saved [{}], appUser ID [{}], role [{}]", savedContract.getText(), savedContract.getAppUserId(), savedContract.getCreatorRole());
    return savedContract;
  }

  @Override
  public Contract save(@NonNull Contract contract) throws NostrException, IOException {
//            TODO: below time needs evolution
    contract.setAgreedCompletionTime(Calendar.getInstance().getTime());
//            TODO: below time needs evolution
    contract.setAgreedStartTime(Calendar.getInstance().getTime());
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
  public Contract constructContract(AppUser appUser) {
    return constructContract(appUser.getId());
  }

  private Contract constructContract(@NonNull Long id) {
    Contract contract = new Contract();
    contract.setAppUserId(id);
    contract.setPayerState(ContractStateEnum.APPROVE);
    contract.setPayeeState(ContractStateEnum.APPROVE);
//            TODO: below time needs evolution
    contract.setAgreedCompletionTime(Calendar.getInstance().getTime());
    return contract;
  }
}
