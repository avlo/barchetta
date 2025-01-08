package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.service.nostr.NostrRelayService;
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
public class ContractEntityServiceNostrDecorator implements ContractEntityServiceNostrDecoratorIF {
  private final ContractEntityServiceIF contractEntityService;
  private final NostrRelayService nostrRelayService;

  @Autowired
  public ContractEntityServiceNostrDecorator(
      @NonNull ContractEntityServiceIF contractEntityService,
      @NonNull NostrRelayService nostrRelayService) {
    this.contractEntityService = contractEntityService;
    this.nostrRelayService = nostrRelayService;
  }

  @Transactional
  @Override
  public Contract save(@NonNull Contract contract) throws IOException, ExecutionException, InterruptedException, NostrException {
    log.info("saving contract {}", contract);
    return nostrRelayService.save(contractEntityService.save(contract));
  }

  @SneakyThrows
  @Override
  public Contract getContract(@NonNull Contract contract) {
    return nostrRelayService.get(contract);
  }

  @Override
  public Contract getContract(@NonNull Long id) {
    return getContract(contractEntityService.getContract(id));
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
    return contractEntityService.getAvailableOppositeRoleContractsByAppUser(appUser).stream()
        .map(this::getContract).toList();
  }

  @Override
  public List<Contract> getContractsByCoParty(@NonNull AppUser coParty) {
    return contractEntityService.getContractsByCoParty(coParty).stream()
        .map(this::getContract).toList();
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull AppUser appUser) {
    return contractEntityService.getContractsByAppUser(appUser).stream()
        .map(this::getContract).toList();
  }

  @Override
  public List<Contract> getAllContracts() {
    return contractEntityService.getAllContracts().stream()
        .map(this::getContract).toList();
  }
}
