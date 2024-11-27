package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import lombok.NonNull;
import nostr.util.NostrException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ControllerServiceIF {
  Contract saveAsCreator(@NonNull ContractDto contract, @NonNull User user) throws IOException, NostrException, ExecutionException, InterruptedException;
  Contract getContractByContractId(@NonNull Long id) throws IOException;
  ContractDto getContractDtoByContractId(@NonNull Long id) throws IOException;
  User findByUsername(@NonNull String username);
  CreatorRoleEnum getRole(Contract contract, User user);
  Contract saveDto(@NonNull ContractDto contractDto) throws NostrException, IOException, ExecutionException, InterruptedException;
  Contract save(@NonNull Contract contract) throws NostrException, IOException, ExecutionException, InterruptedException;
  Contract saveAsCounterParty(@NonNull Long contractId, @NonNull User user) throws NostrException, IOException, ExecutionException, InterruptedException;
  List<Contract> getAll();
  List<Contract> getAllContractsFor(@NonNull User user);
  List<Contract> getOpenContractsFor(@NonNull User user);
  ContractDto constructContractDto();
}
