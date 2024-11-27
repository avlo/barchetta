package com.prosilion.barchetta.service.db;

import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.User;
import com.prosilion.barchetta.repository.ContractRepository;
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
    return contractRepository.save(contract); // should be called after ContractEntityServiceNostrDecorator.save()
  }

  @Override
  public Contract getContractById(@NonNull Long id) {
    return contractRepository.getContractById(id).get();
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull User appUser) {
    return contractRepository.getContractsByAppUserId(appUser.getId());
  }

  @Override
  public List<Contract> getContractsByCoParty(@NonNull User appUser) {
    return contractRepository.getContractsByCoPartyId(appUser.getId());
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull User appUser) {
    return contractRepository.getOpenContractsFor(appUser.getId());
  }

  @Override
  public List<Contract> getAll() {
    return contractRepository.findAll();
  }
}
