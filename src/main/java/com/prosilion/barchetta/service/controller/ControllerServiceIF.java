package com.prosilion.barchetta.service.controller;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.presto.nostr.entity.NostrUser;
import lombok.NonNull;
import nostr.util.NostrException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ControllerServiceIF {
  Contract saveAsCreator(@NonNull ContractDto contract, @NonNull NostrUser user) throws IOException, NostrException, ExecutionException, InterruptedException;
  Contract saveAsCounterParty(@NonNull ContractDto contractDto, @NonNull NostrUser user) throws NostrException, IOException, ExecutionException, InterruptedException;

  Contract update(@NonNull ContractDto contractDto) throws NostrException, IOException, ExecutionException, InterruptedException;
  ContractDto getContractDto(@NonNull Long id);

  List<Contract> getAllContracts() throws IOException;
  List<Contract> getAllUserContracts(@NonNull User user);
  List<Contract> getOpenContracts(@NonNull User user);
  ContractDto constructContractDto();

  User findByUsername(@NonNull String username);
  CreatorRoleEnum getRole(Contract contract, User user);
}
