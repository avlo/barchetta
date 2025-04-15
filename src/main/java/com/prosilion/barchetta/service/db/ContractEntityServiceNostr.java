package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.service.nostr.BarchettaNostrRelayService;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class ContractEntityServiceNostr implements ContractEntityServiceNostrIF {
  private final ContractEntityServiceIF contractEntityService;
  private final BarchettaNostrRelayService barchettaNostrRelayService;

  @Autowired
  public ContractEntityServiceNostr(
      @NonNull ContractEntityServiceIF contractEntityService,
      @NonNull BarchettaNostrRelayService barchettaNostrRelayService) {
    this.contractEntityService = contractEntityService;
    this.barchettaNostrRelayService = barchettaNostrRelayService;
  }

  @Transactional
  @Override
  public Contract save(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException, NostrException {
    log.info("saving contract {}", contract);
    return barchettaNostrRelayService.create(getSaved(contract));
  }

  @Override
  public Contract update(@NonNull Contract contract) throws NostrException, IOException, ExecutionException, InterruptedException {
    return barchettaNostrRelayService.update(getSaved(contract));
  }

  @SneakyThrows
  @Override
  public Contract get(@NonNull Contract contract) {
    return barchettaNostrRelayService.get(contract);
  }

  @Override
  public Contract getContract(@NonNull Long id) {
    return get(contractEntityService.getContract(id));
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
    return contractEntityService.getAvailableOppositeRoleContractsByAppUser(appUser).stream()
        .map(this::get).toList();
  }

  @Override
  public List<Contract> getContractsByCoParty(@NonNull AppUser coParty) {
    return contractEntityService.getContractsByCoParty(coParty).stream()
        .map(this::get).toList();
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull AppUser appUser) {
    return contractEntityService.getContractsByAppUser(appUser).stream()
        .map(this::get).toList();
  }

  @Override
  public List<Contract> getAllContracts() {
    return contractEntityService.getAllContracts().stream()
        .map(this::get).toList();
  }

  private Contract getSaved(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException, NostrException {
    return contractEntityService.save(contract);
  }
}
