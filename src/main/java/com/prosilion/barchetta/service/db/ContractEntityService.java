package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.repository.ContractRepository;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractEntityService implements ContractEntityServiceIF {
  private final ContractRepository contractRepository;

  @Autowired
  public ContractEntityService(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;
  }

  @Transactional
  @Override
  public Contract save(@NonNull Contract contract) {
    return contractRepository.save(contract);
  }

  @Override
  public Contract getContract(@NonNull Long contractId) {
    return contractRepository.getContractById(contractId).get();
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull AppUser appUser) {
    return contractRepository.getContractsByAppUserId(appUser.getId());
  }

  @Override
  public List<Contract> getContractsByCoParty(@NonNull AppUser appUser) {
    return contractRepository.getContractsByCoPartyId(appUser.getId());
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
    return contractRepository.getOpenContractsFor(appUser.getId());
  }

  @Override
  public List<Contract> getAllContracts() {
    return contractRepository.findAll();
  }
}
