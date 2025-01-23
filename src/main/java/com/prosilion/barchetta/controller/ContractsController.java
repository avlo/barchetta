package com.prosilion.barchetta.controller;

import com.prosilion.barchetta.model.dto.ContractDto;
import com.prosilion.barchetta.model.entity.Contract;
import com.prosilion.barchetta.model.entity.CreatorRoleEnum;
import com.prosilion.barchetta.service.nostr.NostrControllerServiceIF;
import com.prosilion.presto.nostr.entity.NostrUser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.util.NostrException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Slf4j
@Controller
@RequestMapping("/contract")
public class ContractsController {
  public static final String CONTRACT_DTO_STR = "contractDto";
  public static final String CONTRACTS_STR = "contracts";
  public static final String COUNTER_PARTY_ID_STR = "counter_party_id";
  public static final String CTB_EVENT_ID = "ctbEventId";
  public static final String OPEN_CONTRACTS_STR = "open_contracts";
  public static final String USER_CONTRACTS_STR = "user_contracts";
  public static final String VIEWER_ROLE_STR = "viewer_role";

  private final NostrControllerServiceIF nostrControllerService;

  @Autowired
  public ContractsController(@NonNull NostrControllerServiceIF nostrControllerService) {
    this.nostrControllerService = nostrControllerService;
  }

  @GetMapping({"/index.html", "/"})
  public String redirectIndex() {
    return "forward:/register";
  }

  //  TODO: below security not being applied, needs investigation
  @Secured({"ROLE_USER", "USER"})
  @PostMapping("/create")
  public String createContract(@AuthenticationPrincipal NostrUser user, ContractDto contractDto, Model model) throws IOException, NostrException, ExecutionException, InterruptedException {
    nostrControllerService.createContract(contractDto, user);
    setCanonicalModelAttributes(user, model);
    return "thymeleaf/contract/display_all";
  }

  //  TODO: below security not being applied, needs investigation
  @Secured({"ROLE_USER", "USER"})
  @PostMapping("/update")
  public String applyForContract(@AuthenticationPrincipal NostrUser user, ContractDto contractDto, Model model) throws IOException, NostrException, ExecutionException, InterruptedException {
    Contract contract = nostrControllerService.createContractCounterparty(contractDto, user);
    model.addAttribute(CTB_EVENT_ID, contract.getNostrCalendarTimeBasedEventId());
    setCanonicalModelAttributes(user, model);
    return "thymeleaf/contract/display_all";
  }

  @Secured({"ROLE_USER", "USER"})
  @GetMapping("/display_all")
  public String showUserContracts(@AuthenticationPrincipal NostrUser user, Model model) {
    setCanonicalModelAttributes(user, model);
    return "thymeleaf/contract/display_all";
  }

  @GetMapping("/display_contract/{id}")
  public String showAvailableContracts(@AuthenticationPrincipal NostrUser user, @PathVariable("id") Long contractId, Model model) {
    log.info("Fetching selected contract: [{}]", contractId);
    model.addAttribute(
        CONTRACT_DTO_STR,
        nostrControllerService.getContractDto(contractId));
    model.addAttribute(
        COUNTER_PARTY_ID_STR,
        nostrControllerService.findByUsername(user.getUsername()).getId());
    log.info("CounterPartyId: [{}]", nostrControllerService.findByUsername(user.getUsername()).getId());
    log.info("User for potential contract: {}", user.getUsername());
    return "thymeleaf/contract/contract_application_form";
  }

  @GetMapping("/my_contract/{id}")
  public String showMyContracts(@AuthenticationPrincipal NostrUser user, @PathVariable("id") Long contractId, Model model) {
    log.info("Fetching my contract: [{}]", contractId);
    ContractDto contractDto = nostrControllerService.getContractDto(contractId);
    model.addAttribute(
        CONTRACT_DTO_STR,
        contractDto);
    model.addAttribute(
        VIEWER_ROLE_STR,
        setViewerRole(user, contractDto));
    return "thymeleaf/contract/view_contract";
  }

  @PostMapping("/vote")
  public String voteOnContract(@AuthenticationPrincipal NostrUser user, ContractDto contractDto, Model model) throws IOException, NostrException, ExecutionException, InterruptedException {
    log.info("User [{}] voting on contractDto [{}]", user.getUsername(), contractDto);
    log.info("Contract id: [{}] ", contractDto.getId());
    log.info("Contract text: [{}] ", contractDto.getText());
    log.info("Contract appUserId: [{}] ", contractDto.getAppUserId());
    nostrControllerService.update(contractDto);
    model.addAttribute(CONTRACTS_STR, nostrControllerService.getAllContracts());
    return "redirect:display_all";
  }

  private void setCanonicalModelAttributes(@NonNull NostrUser nostrUser, @NonNull Model model) {
    model.addAttribute(USER_CONTRACTS_STR, nostrControllerService.getAllNostrUserContracts(nostrUser));
    model.addAttribute(OPEN_CONTRACTS_STR, nostrControllerService.getOpenContracts(nostrUser));
    model.addAttribute(CONTRACT_DTO_STR, nostrControllerService.constructContractDto());
    log.debug("model:\n  {}\n\n", model.toString());
  }

  private CreatorRoleEnum setViewerRole(@AuthenticationPrincipal NostrUser user, ContractDto contractDto) {
    return user.getPubkey().equals(contractDto.getNostrAppUserPubKey()) ? contractDto.getCreatorRole() : CreatorRoleEnum.getOppositeRole(contractDto.getCreatorRole());
  }
}
