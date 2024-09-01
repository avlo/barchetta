//package com.prosilion.barchetta.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.prosilion.barchetta.client.NostrWebSocketClient;
//import com.prosilion.barchetta.model.entity.Contract;
//import com.prosilion.barchetta.repository.ContractRepository;
//import com.prosilion.presto.security.entity.AppUser;
//import jakarta.transaction.Transactional;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import nostr.api.factory.impl.NIP01Impl.EventMessageFactory;
//import nostr.event.impl.ClassifiedListingEvent;
//import nostr.event.impl.Filters;
//import nostr.event.message.EventMessage;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Slf4j
//@Service
//public class ContractNostrService {
//  private final ContractRepository contractRepository;
//  private final NostrWebSocketClient nostrWebSocketClient;
//
//  @Autowired
//  public ContractNostrService(ContractRepository contractRepository, NostrWebSocketClient nostrWebSocketClient) {
//    this.contractRepository = contractRepository;
//    this.nostrWebSocketClient = nostrWebSocketClient;
//  }
//
//  @Transactional
//  public Contract save(@NonNull Contract contractEntity) throws JsonProcessingException {
//    log.info("saving contract {}", contractEntity);
//
//    postContract(contractEntity);
//    return contractEntity;
//  }
//
//  private void postContract(Contract contractEntity) throws JsonProcessingException {
////    EventMessage eventMessage = new EventMessageFactory(contract.getClassifiedListingEvent(), contract.getNostrAppUserId()).create();
////    nostrWebSocketClient.send(eventMessage);
//  }
//
//  public ContractEntity getContractById(@NonNull Long id) throws JsonProcessingException {
////    ContractEntity contractEntity = contractRepository.getContractById(id).get();
////    ClassifiedListingEvent contractByEventId = getContractByEventId(contractEntity);
////    nostrClientServiceString.getClassifiedListingEvent(NostrClientService.nostrRequestString);
////    Message<T> classifiedListingEventString = nostrClientServiceString.getReturnVal();
////    nostrClientService.getClassifiedListingEvent(NostrClientService.nostrRequestString);
////    Message<U> classifiedListingEvent = nostrClientService.getReturnVal();
////    contract.setText(classifiedListingEvent.getPayload().getContent());
////    PublicKey publicKey = classifiedListingEvent.getPayload().getPubKey();
////    contract.setNostrAppUserId(publicKey.toHexString());
////    return contractEntity;
//  }
//
//  private ClassifiedListingEvent getContractByEventId(ContractEntity contractEntity) throws JsonProcessingException {
////    Contract contract = contractRepository.getContractById(eventId).get();
//    Filters filters = new Filters();
////    filters.setEvents(List.of(contract.getClassifiedListingEvent()));
////    TODO: hook in callback below...
////    nostrWebSocketClient.send(new ReqMessage(contract.getNostrAppUserId(), filters));
////    TODO: then replace null
//    return null;
//  }
//
//  public List<ContractEntity> getContractsByAppUser(@NonNull AppUser appUser) {
//    return getContractsByAppUserId(appUser.getId());
//  }
//
//  public List<ContractEntity> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
//    return getAvailableOppositeRoleContractsByAppUserId(appUser.getId());
//  }
//
//  public List<ContractEntity> getContractsByCoPartyId(@NonNull Long id) {
////    return contractRepository.getContractsByCoPartyId(id);
//  }
//
//  public List<ContractEntity> getAvailableOppositeRoleContractsByAppUserId(@NonNull Long id) {
////    return contractRepository.getOpenContractsFor(id);
//  }
//
//  public List<ContractEntity> getContractsByAppUserId(@NonNull Long id) {
////    return contractRepository.getContractsByAppUserId(id);
//  }
//
//  public List<ContractEntity> getAll() {
//    return contractRepository.findAll();
//  }
//}
