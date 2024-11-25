package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.presto.nostr.entity.NostrUser;
import com.prosilion.presto.security.entity.AppUser;
import lombok.NonNull;
import nostr.util.NostrException;

import java.io.IOException;
import java.util.List;

public interface ControllerServiceIF {
  Contract createContract(@NonNull ContractDto contract, @NonNull NostrUser user) throws IOException, NostrException;
  Contract getContractByContractId(@NonNull Long id) throws IOException;
  ContractDto getContractDtoByContractId(@NonNull Long id) throws IOException;
  User findByUsername(@NonNull String username);
  CreatorRoleEnum getRole(Contract contract, User user);
  Contract saveDto(@NonNull ContractDto contractDto) throws NostrException, IOException;
  Contract save(@NonNull Contract contract) throws NostrException, IOException;
  Contract saveAsCounterParty(@NonNull Long contractId, @NonNull NostrUser user) throws NostrException, IOException;
  List<Contract> getAll();
  List<Contract> getAllContractsFor(@NonNull AppUser appUser);
  List<Contract> getOpenContractsFor(@NonNull AppUser appUser);
  ContractDto constructContractDto(AppUser appUser);
}
