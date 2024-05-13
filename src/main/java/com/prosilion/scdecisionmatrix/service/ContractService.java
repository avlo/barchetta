package com.prosilion.scdecisionmatrix.service;

import com.prosilion.scdecisionmatrix.model.entity.Contract;
import com.prosilion.scdecisionmatrix.repository.ContractRepository;
import com.prosilion.scdecisionmatrix.service.nostr.NostrClientService;
import com.prosilion.scdecisionmatrix.service.nostr.NostrClientServiceImpl;
import edu.mayo.lpea.cad.cadence3.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.event.impl.ClassifiedListingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService<T extends String, U extends ClassifiedListingEvent> {
  private final ContractRepository contractRepository;
  private final NostrClientServiceImpl<T> nostrClientServiceString;
  private final NostrClientServiceImpl<U> nostrClientService;


  //  @Value("${nostr.file.nostr-request-string.json}")
  //  private String nostrRequestString;

  @Autowired
  public ContractService(ContractRepository contractRepository, NostrClientServiceImpl<T> nostrClientServiceString, NostrClientServiceImpl<U> nostrClientService) {
    this.contractRepository = contractRepository;
    this.nostrClientServiceString = nostrClientServiceString;
    this.nostrClientService = nostrClientService;

  }

  @Transactional
  public Contract save(@NonNull Contract contract) {
//    nostrClientServiceString.getClassifiedListingEvent(NostrClientService.nostrRequestString);
//    Message<T> classifiedListingEventString = nostrClientServiceString.getReturnVal();
//    nostrClientService.getClassifiedListingEvent(NostrClientService.nostrRequestString);
//    Message<U> classifiedListingEvent = nostrClientService.getReturnVal();
//
//    contract.setText(classifiedListingEvent.getPayload().getContent());
//    PublicKey publicKey = classifiedListingEvent.getPayload().getPubKey();
//    contract.setNostrAppUserId(publicKey.toHexString());
    return contractRepository.save(contract);
  }

  public Contract getContractById(@NonNull Long id) {
    Contract contract = contractRepository.getContractById(id).get();
//    nostrClientServiceString.getClassifiedListingEvent(NostrClientService.nostrRequestString);
//    Message<T> classifiedListingEventString = nostrClientServiceString.getReturnVal();
//    nostrClientService.getClassifiedListingEvent(NostrClientService.nostrRequestString);
//    Message<U> classifiedListingEvent = nostrClientService.getReturnVal();
//    contract.setText(classifiedListingEvent.getPayload().getContent());
//    PublicKey publicKey = classifiedListingEvent.getPayload().getPubKey();
//    contract.setNostrAppUserId(publicKey.toHexString());
    return contract;
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
