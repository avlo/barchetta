package com.prosilion.barchetta.service;

import com.prosilion.barchetta.client.NostrWebSocketClient;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.presto.security.entity.AppUser;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.event.Kind;
import nostr.event.impl.ClassifiedListing;
import nostr.event.impl.ClassifiedListingEvent;
import nostr.event.tag.PriceTag;
import nostr.id.Identity;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ContractServiceNostrDecorator implements ContractServiceIF, Subscriber<String> {
  private final NostrWebSocketClient nostrWebSocketClient;
  private final ContractService contractService;
  private static final String SUBSCRIBER = "ReqClientComponent-ID-001";
  //  private static final String REQ_CLIENT_EVENT_ID_001 = "8f66a36101d3d152c6270e18f5622d1f8bce4ac5da9ab62d7c3cc0006e5914cc";
//  private static final String idReqJson = "[\"REQ\",\"" + REQ_CLIENT_SUBSCRIBER_001 + "\",{\"ids\":[\"" + REQ_CLIENT_EVENT_ID_001 + "\"]}]";

  public ContractServiceNostrDecorator(ContractService contractService, NostrWebSocketClient nostrWebSocketClient) {
    this.contractService = contractService;
    this.nostrWebSocketClient = nostrWebSocketClient;
  }

  @Transactional
  @Override
  @SneakyThrows
  public Contract save(@NonNull Contract contract) {
//    log.info("saving contract {}", contract);
//    EventMessage eventMessage = new EventMessageFactory(convertToClassifiedListingEvent(contract), contract.getNostrAppUserId()).create();
//    nostrWebSocketClient.send(eventMessage).subscribeWith(this);
//    return contract;
    return contractService.save(contract);
  }

  @Override
  public Contract getContractById(@NonNull Long id) {
//    return convertToContract(getContractById(id.toString()));
    return contractService.getContractById(id);
  }

  @Override
  public List<Contract> getContractsByAppUser(@NonNull AppUser appUser) {
//    return getContractsByAppUserId(appUser.getId());
    return contractService.getContractsByAppUser(appUser);
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUser(@NonNull AppUser appUser) {
//    return getAvailableOppositeRoleContractsByAppUserId(appUser.getId());
    return contractService.getAvailableOppositeRoleContractsByAppUser(appUser);
  }

  @Override
  public List<Contract> getContractsByCoPartyId(@NonNull Long id) {
//    return getContractsByAppUserId(id);
    return contractService.getContractsByCoPartyId(id);
  }

  @Override
  public List<Contract> getAvailableOppositeRoleContractsByAppUserId(@NonNull Long id) {
//    return getContractsByAppUserId(id);
    return contractService.getAvailableOppositeRoleContractsByAppUserId(id);
  }

  @Override
  public List<Contract> getContractsByAppUserId(@NonNull Long id) {
//    return List.of(getContractById(id));
    return contractService.getContractsByAppUserId(id);
  }

  @Override
  public List<Contract> getAll() {
//    String allContracts = "[\"REQ\",\"" + SUBSCRIBER + "\",{\"kind\":[\"" + Kind.CLASSIFIED_LISTING + "\"]}]";
//    return List.of(convertToContract(getContractById(allContracts)));
    return contractService.getAll();
  }

  private String createReqJson(String id) {
    return "[\"REQ\",\"" + SUBSCRIBER + "\",{\"ids\":[\"" + id + "\"]}]";
  }

  private ClassifiedListingEvent convertToClassifiedListingEvent(Contract contract) {
    return new ClassifiedListingEvent(
        Identity.create("myprivatekey").getPublicKey(),
        Kind.CLASSIFIED_LISTING,
        new ArrayList<>(),
        "CONTENT",
        new ClassifiedListing(
            contract.getText(),
            "SUMMARY",
            new PriceTag(BigDecimal.TEN, "btc", "once")));
  }

  private ClassifiedListingEvent getContractById(String id) {
    nostrWebSocketClient.send(createReqJson(id)).subscribeWith(this);
    return null;
  }

  private Contract convertToContract(ClassifiedListingEvent classifiedListingEvent) {
    return new Contract();
  }

  @Override
  public void onSubscribe(Subscription subscription) {
    System.out.println("000000000000000000");
    System.out.println("000000000000000000");
    System.out.println("subscribed");
    System.out.println("000000000000000000");
    System.out.println("000000000000000000");
  }

  @Override
  public void onNext(String s) {
    System.out.println("11111111111111111111111111");
    System.out.println("11111111111111111111111111");
    System.out.println(s);
    System.out.println("11111111111111111111111111");
    System.out.println("11111111111111111111111111");
  }

  @Override
  public void onError(Throwable throwable) {
    System.out.println("222222222222222222");
    System.out.println("222222222222222222");
    System.out.println("error");
    System.out.println("222222222222222222");
    System.out.println("222222222222222222");
  }

  @Override
  public void onComplete() {
    System.out.println("333333333333333333");
    System.out.println("333333333333333333");
    System.out.println("completed");
    System.out.println("333333333333333333");
    System.out.println("333333333333333333");
  }
}
