package com.prosilion.barchetta.service;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.repository.ContractRepository;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService implements ContractServiceIF {
  private final ContractRepository contractRepository;

  @Autowired
  public ContractService(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;
  }

  @Transactional
  @Override
  public Contract save(@NonNull Contract contract) {
    return contractRepository.save(contract);
  }

  @Override
  public Contract getContractById(@NonNull Long id) {
    return contractRepository.getContractById(id).get();
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull AppUser appUser) {
    return getContractsByAppUserId(appUser.getId());
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
    return getAvailableOppositeRoleContractsByAppUserId(appUser.getId());
  }

  @Override
  public List<Contract> getContractsByCoPartyId(@NonNull Long id) {
    return contractRepository.getContractsByCoPartyId(id);
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUserId(@NonNull Long id) {
    return contractRepository.getOpenContractsFor(id);
  }

  @Override
  public List<Contract> getContractsByAppUserId(@NonNull Long id) {
    return contractRepository.getContractsByAppUserId(id);
  }

  @Override
  public List<Contract> getAll() {
    return contractRepository.findAll();
  }
}
