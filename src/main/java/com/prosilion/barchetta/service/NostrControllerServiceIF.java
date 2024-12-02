package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.presto.nostr.entity.NostrUser;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface NostrControllerServiceIF extends ControllerServiceIF {
  List<Contract> getAllNostrUserContracts(@NonNull NostrUser user);
  List<Contract> getOpenContracts(@NonNull NostrUser nostrUser);
  Contract getContract(@NonNull Long id, @NotNull NostrUser user);
  ContractDto getContractDto(@NonNull Long id, @NonNull NostrUser nostrUser);
}
