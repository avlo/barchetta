package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.repository.ContractRepository;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import nostr.event.impl.ClassifiedListingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService<T extends String, U extends ClassifiedListingEvent> {
  private final ContractRepository contractRepository;

  @Autowired
  public ContractService(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;

  }

  @Transactional
  public Contract save(@NonNull Contract contract) {
    return contractRepository.save(contract);
  }

  public Contract getContractById(@NonNull Long id) {
    return contractRepository.getContractById(id).get();
  }

  public List<Contract> getContractsByAppUser(@NonNull AppUser appUser) {
    return getContractsByAppUserId(appUser.getId());
  }

  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
    return getAvailableOppositeRoleContractsByAppUserId(appUser.getId());
  }

  public List<Contract> getContractsByCoPartyId(@NonNull Long id) {
    return contractRepository.getContractsByCoPartyId(id);
  }

  public List<Contract> getAvailableOppositeRoleContractsByAppUserId(@NonNull Long id) {
    return contractRepository.getOpenContractsFor(id);
  }

  public List<Contract> getContractsByAppUserId(@NonNull Long id) {
    return contractRepository.getContractsByAppUserId(id);
  }

  public List<Contract> getAll() {
    return contractRepository.findAll();
  }
}
